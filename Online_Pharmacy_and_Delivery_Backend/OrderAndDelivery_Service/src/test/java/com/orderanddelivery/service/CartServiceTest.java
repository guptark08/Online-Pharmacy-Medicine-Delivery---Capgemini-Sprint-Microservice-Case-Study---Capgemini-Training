package com.orderanddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.orderanddelivery.entities.CartItem;
import com.orderanddelivery.exception.ResourceNotFoundException;
import com.orderanddelivery.integration.CatalogClient;
import com.orderanddelivery.integration.CatalogMedicineSnapshot;
import com.orderanddelivery.repository.CartItemRepository;
import com.orderanddelivery.requestDTO.AddToCartRequest;
import com.orderanddelivery.responseDTO.CartResponse;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCart_newItem_fetchesSnapshotFromCatalogAndSaves() {
        AddToCartRequest request = new AddToCartRequest();
        request.setMedicineId(1L);
        request.setQuantity(2);
        request.setSubstituteAllowed(true);

        CatalogMedicineSnapshot snapshot = new CatalogMedicineSnapshot(
                1L,
                "Paracetamol",
                new BigDecimal("25.00"),
                false);

        when(catalogClient.fetchMedicineSnapshot(1L)).thenReturn(snapshot);
        when(cartItemRepository.findByUserIdAndMedicineId(10L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItem saved = cartService.addToCart(10L, request);

        assertEquals("Paracetamol", saved.getMedicineName());
        assertEquals(new BigDecimal("25.00"), saved.getUnitPrice());
        assertEquals(2, saved.getQuantity());
    }

    @Test
    void addToCart_existingItem_mergesQuantity() {
        AddToCartRequest request = new AddToCartRequest();
        request.setMedicineId(1L);
        request.setQuantity(2);
        request.setSubstituteAllowed(true);

        CartItem existing = CartItem.builder()
                .id(5L)
                .userId(10L)
                .medicineId(1L)
                .medicineName("Old Name")
                .unitPrice(new BigDecimal("20.00"))
                .quantity(3)
                .requiresPrescription(true)
                .substituteAllowed(false)
                .build();

        CatalogMedicineSnapshot snapshot = new CatalogMedicineSnapshot(
                1L,
                "Paracetamol",
                new BigDecimal("25.00"),
                false);

        when(catalogClient.fetchMedicineSnapshot(1L)).thenReturn(snapshot);
        when(cartItemRepository.findByUserIdAndMedicineId(10L, 1L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItem saved = cartService.addToCart(10L, request);

        assertEquals(5, saved.getQuantity());
        assertEquals("Paracetamol", saved.getMedicineName());
        assertEquals(new BigDecimal("25.00"), saved.getUnitPrice());
    }

    @Test
    void addToCart_mergedQuantityExceedsMax_throwsIllegalArgument() {
        AddToCartRequest request = new AddToCartRequest();
        request.setMedicineId(1L);
        request.setQuantity(3);

        CartItem existing = CartItem.builder()
                .id(5L)
                .userId(10L)
                .medicineId(1L)
                .medicineName("Paracetamol")
                .unitPrice(new BigDecimal("25.00"))
                .quantity(8)
                .build();

        CatalogMedicineSnapshot snapshot = new CatalogMedicineSnapshot(
                1L,
                "Paracetamol",
                new BigDecimal("25.00"),
                false);

        when(catalogClient.fetchMedicineSnapshot(1L)).thenReturn(snapshot);
        when(cartItemRepository.findByUserIdAndMedicineId(10L, 1L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(10L, request));
    }

    @Test
    void addToCart_quantityZero_throwsIllegalArgument() {
        AddToCartRequest request = new AddToCartRequest();
        request.setMedicineId(1L);
        request.setQuantity(0);

        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(10L, request));
        verifyNoInteractions(catalogClient);
    }

    @Test
    void updateQuantity_setToZero_deletesItem() {
        CartItem item = CartItem.builder()
                .id(7L)
                .userId(10L)
                .medicineId(1L)
                .medicineName("Paracetamol")
                .unitPrice(new BigDecimal("25.00"))
                .quantity(2)
                .build();

        when(cartItemRepository.findByIdAndUserId(7L, 10L)).thenReturn(Optional.of(item));

        cartService.updateQuantity(10L, 7L, 0);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void updateQuantity_wrongOwner_throwsResourceNotFound() {
        when(cartItemRepository.findByIdAndUserId(99L, 50L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.updateQuantity(50L, 99L, 2));
    }

    @Test
    void getCart_calculatesSubtotalAndDetectsRxItems() {
        CartItem first = CartItem.builder()
                .id(1L)
                .userId(10L)
                .medicineId(101L)
                .medicineName("Vitamin C")
                .unitPrice(new BigDecimal("10"))
                .quantity(2)
                .requiresPrescription(false)
                .build();

        CartItem second = CartItem.builder()
                .id(2L)
                .userId(10L)
                .medicineId(102L)
                .medicineName("Antibiotic")
                .unitPrice(new BigDecimal("30"))
                .quantity(1)
                .requiresPrescription(true)
                .build();

        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of(first, second));

        CartResponse response = cartService.getCart(10L);

        assertEquals(0, new BigDecimal("50").compareTo(response.getSubtotal()));
        assertTrue(response.isHasRxItems());
        assertEquals(3, response.getTotalItems());
    }

    @Test
    void clearCart_deletesAllUserItems() {
        cartService.clearCart(10L);
        verify(cartItemRepository, times(1)).deleteByUserId(10L);
    }
}
