package com.orderanddelivery.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderanddelivery.entities.CartItem;
import com.orderanddelivery.exception.ResourceNotFoundException;
import com.orderanddelivery.integration.CatalogClient;
import com.orderanddelivery.integration.CatalogMedicineSnapshot;
import com.orderanddelivery.repository.CartItemRepository;
import com.orderanddelivery.requestDTO.AddToCartRequest;
import com.orderanddelivery.responseDTO.CartResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 10;

    private final CartItemRepository cartItemRepository;
    private final CatalogClient catalogClient;

    @Transactional
    public CartItem addToCart(Long userId, AddToCartRequest request) {
        validateQuantity(request.getQuantity());

        CatalogMedicineSnapshot snapshot = catalogClient.fetchMedicineSnapshot(request.getMedicineId());
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndMedicineId(userId, request.getMedicineId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int updatedQuantity = item.getQuantity() + request.getQuantity();
            validateQuantity(updatedQuantity);

            applyCatalogSnapshot(item, snapshot);
            item.setQuantity(updatedQuantity);
            item.setSubstituteAllowed(request.isSubstituteAllowed());
            return cartItemRepository.save(item);
        }

        CartItem newItem = CartItem.builder()
                .userId(userId)
                .medicineId(snapshot.medicineId())
                .medicineName(snapshot.medicineName())
                .unitPrice(snapshot.unitPrice())
                .quantity(request.getQuantity())
                .requiresPrescription(snapshot.requiresPrescription())
                .substituteAllowed(request.isSubstituteAllowed())
                .build();

        try {
            return cartItemRepository.save(newItem);
        } catch (DataIntegrityViolationException ex) {
            CartItem concurrentItem = cartItemRepository.findByUserIdAndMedicineId(userId, request.getMedicineId())
                    .orElseThrow(() -> ex);

            int mergedQuantity = concurrentItem.getQuantity() + request.getQuantity();
            validateQuantity(mergedQuantity);

            applyCatalogSnapshot(concurrentItem, snapshot);
            concurrentItem.setQuantity(mergedQuantity);
            concurrentItem.setSubstituteAllowed(request.isSubstituteAllowed());
            return cartItemRepository.save(concurrentItem);
        }
    }

    public CartResponse getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        List<CartResponse.CartItemResponse> itemResponses = items.stream()
                .map(this::toCartItemResponse)
                .toList();

        BigDecimal subtotal = itemResponses.stream()
                .map(CartResponse.CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean hasRxItems = items.stream().anyMatch(CartItem::isRequiresPrescription);

        CartResponse cart = new CartResponse();
        cart.setItems(itemResponses);
        cart.setTotalItems(items.stream().mapToInt(CartItem::getQuantity).sum());
        cart.setSubtotal(subtotal);
        cart.setHasRxItems(hasRxItems);
        return cart;
    }

    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }

        validateQuantity(quantity);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartResponse.CartItemResponse toCartItemResponse(CartItem item) {
        CartResponse.CartItemResponse response = new CartResponse.CartItemResponse();
        response.setId(item.getId());
        response.setMedicineId(item.getMedicineId());
        response.setMedicineName(item.getMedicineName());
        response.setUnitPrice(item.getUnitPrice());
        response.setQuantity(item.getQuantity());
        response.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        response.setRequiresPrescription(item.isRequiresPrescription());
        response.setSubstituteAllowed(item.isSubstituteAllowed());
        return response;
    }

    private void applyCatalogSnapshot(CartItem item, CatalogMedicineSnapshot snapshot) {
        item.setMedicineName(snapshot.medicineName());
        item.setUnitPrice(snapshot.unitPrice());
        item.setRequiresPrescription(snapshot.requiresPrescription());
    }

    private void validateQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity per item must be between 1 and 10");
        }
    }
}
