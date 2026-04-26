package com.orderanddelivery.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderanddelivery.entities.CartItem;
import com.orderanddelivery.entities.Order;
import com.orderanddelivery.entities.OrderItem;
import com.orderanddelivery.enums.OrderStatus;
import com.orderanddelivery.exception.InvalidOrderStateException;
import com.orderanddelivery.integration.AddressClient;
import com.orderanddelivery.integration.CatalogClient;
import com.orderanddelivery.messaging.DomainEventPublisher;
import com.orderanddelivery.messaging.PharmacyEventRoutingKeys;
import com.orderanddelivery.messaging.events.OrderCreatedEvent;
import com.orderanddelivery.repository.CartItemRepository;
import com.orderanddelivery.repository.OrderRepository;
import com.orderanddelivery.requestDTO.CheckoutRequest;
import com.orderanddelivery.responseDTO.OrderResponse;
import com.orderanddelivery.responseDTO.UserAddressResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CatalogClient catalogClient;
    private final AddressClient addressClient;
    private final ObjectProvider<DomainEventPublisher> domainEventPublisherProvider;

    @Transactional
    public OrderResponse startCheckout(Long userId, String bearerToken, CheckoutRequest request) {

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new InvalidOrderStateException("Cart is empty. Add items before checkout.");
        }

        UserAddressResponse address = resolveAddressForCheckout(request, bearerToken);

        boolean hasPrescriptionItems = cartItems.stream().anyMatch(CartItem::isRequiresPrescription);
        OrderStatus initialStatus = OrderStatus.PAYMENT_PENDING;
        Long prescriptionIdToSave = null;

        if (hasPrescriptionItems) {
            if (request.getPrescriptionId() == null) {
                throw new InvalidOrderStateException("Prescription is required for items in your cart.");
            }

            String prescriptionStatus = catalogClient.fetchPrescriptionStatusForUser(
                    request.getPrescriptionId(),
                    bearerToken);

            initialStatus = resolveOrderStatusFromPrescriptionStatus(prescriptionStatus);
            prescriptionIdToSave = request.getPrescriptionId();
        }

        catalogClient.reserveInventoryForCheckout(cartItems, bearerToken);

        BigDecimal totalAmount = calculateCartTotal(cartItems);

        Order order = Order.builder()
                .userId(userId)
                .status(initialStatus)
                .deliveryAddress(buildDeliveryAddress(address))
                .deliveryPincode(address.getPincode() == null ? null : String.valueOf(address.getPincode()))
                 .deliveryPhone(address.getContactPhone())
                .deliverySlot(request.getDeliverySlot())
                .prescriptionId(prescriptionIdToSave)
                .totalAmount(totalAmount)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(totalAmount)
                .build();

        List<OrderItem> orderItems = buildOrderItems(order, cartItems);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        linkPrescriptionToOrderIfRequired(savedOrder, bearerToken);
        cartService.clearCart(userId);
        publishOrderCreatedEvent(savedOrder, orderItems);

        return buildOrderResponse(savedOrder, orderItems);
    }

    private UserAddressResponse resolveAddressForCheckout(CheckoutRequest request, String bearerToken) {
        boolean hasAddressId = request.getAddressId() != null;
        boolean hasNewAddress = request.getNewAddress() != null;

        if (hasAddressId && hasNewAddress) {
            throw new IllegalArgumentException("Provide either addressId or newAddress, not both");
        }

        if (!hasAddressId && !hasNewAddress) {
            throw new IllegalArgumentException("Address ID or newAddress is required");
        }

        if (hasNewAddress) {
            return addressClient.addAddressForCurrentUser(request.getNewAddress(), bearerToken);
        }

        return addressClient.getAddressByIdForCurrentUser(request.getAddressId(), bearerToken);
    }

    private OrderStatus resolveOrderStatusFromPrescriptionStatus(String prescriptionStatus) {
        String normalized = prescriptionStatus == null ? "UNKNOWN" : prescriptionStatus.toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "APPROVED", "VERIFIED" -> OrderStatus.PRESCRIPTION_APPROVED;
            case "PENDING", "UNDER_REVIEW", "IN_REVIEW", "SUBMITTED" -> OrderStatus.PRESCRIPTION_PENDING;
            case "REJECTED", "EXPIRED", "CANCELLED" ->
                throw new InvalidOrderStateException("Prescription is not approved. Current status: " + normalized);
            default -> throw new InvalidOrderStateException("Unsupported prescription status: " + normalized);
        };
    }

    private BigDecimal calculateCartTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderItem> buildOrderItems(Order order, List<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> {
                    BigDecimal subtotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

                    return OrderItem.builder()
                            .order(order)
                            .medicineId(cartItem.getMedicineId())
                            .medicineName(cartItem.getMedicineName())
                            .unitPrice(cartItem.getUnitPrice())
                            .quantity(cartItem.getQuantity())
                            .subtotal(subtotal)
                            .requiresPrescription(cartItem.isRequiresPrescription())
                            .build();
                })
                .toList();
    }

    private String buildDeliveryAddress(UserAddressResponse address) {
        StringBuilder builder = new StringBuilder();
        builder.append(address.getStreetAddress());

        builder.append(", ")
                .append(address.getCity())
                .append(", ")
                .append(address.getState());

        return builder.toString();
    }

    private void publishOrderCreatedEvent(Order savedOrder, List<OrderItem> orderItems) {
        if (domainEventPublisherProvider == null) {
            return;
        }
        DomainEventPublisher domainEventPublisher = domainEventPublisherProvider.getIfAvailable();
        if (domainEventPublisher == null) {
            return;
        }

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(OffsetDateTime.now().toString())
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .orderStatus(savedOrder.getStatus().name())
                .totalAmount(savedOrder.getTotalAmount())
                .finalAmount(savedOrder.getFinalAmount())
                .itemCount(orderItems.size())
                .prescriptionId(savedOrder.getPrescriptionId())
                .deliveryPincode(savedOrder.getDeliveryPincode())
                .build();

        domainEventPublisher.publishAfterCommit(PharmacyEventRoutingKeys.ORDER_CREATED, event);
    }


    private void linkPrescriptionToOrderIfRequired(Order savedOrder, String bearerToken) {
        if (savedOrder.getPrescriptionId() == null) {
            return;
        }

        catalogClient.linkPrescriptionToOrder(savedOrder.getPrescriptionId(), savedOrder.getId(), bearerToken);
    }
    private OrderResponse buildOrderResponse(Order order, List<OrderItem> items) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setDeliverySlot(order.getDeliverySlot());
        response.setTotalAmount(order.getTotalAmount());
        response.setFinalAmount(order.getFinalAmount());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderResponse.OrderItemResponse> itemResponses = items.stream()
                .map(item -> {
                    OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
                    itemResponse.setMedicineId(item.getMedicineId());
                    itemResponse.setMedicineName(item.getMedicineName());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setUnitPrice(item.getUnitPrice());
                    itemResponse.setTotalPrice(item.getSubtotal());
                    return itemResponse;
                })
                .toList();

        response.setItems(itemResponses);
        return response;
    }
}


