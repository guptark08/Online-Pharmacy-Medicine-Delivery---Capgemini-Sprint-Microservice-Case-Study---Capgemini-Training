package com.orderanddelivery.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderanddelivery.entities.Order;
import com.orderanddelivery.entities.OrderItem;
import com.orderanddelivery.enums.OrderStatus;
import com.orderanddelivery.exception.InvalidOrderStateException;
import com.orderanddelivery.exception.ResourceNotFoundException;
import com.orderanddelivery.messaging.DomainEventPublisher;
import com.orderanddelivery.messaging.PharmacyEventRoutingKeys;
import com.orderanddelivery.messaging.events.OrderStatusChangedEvent;
import com.orderanddelivery.repository.OrderItemRepository;
import com.orderanddelivery.repository.OrderRepository;
import com.orderanddelivery.requestDTO.AddToCartRequest;
import com.orderanddelivery.responseDTO.CartResponse;
import com.orderanddelivery.responseDTO.OrderResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<OrderStatus> CUSTOMER_CANCELLABLE_STATUSES = Set.of(
            OrderStatus.CHECKOUT_STARTED,
            OrderStatus.PRESCRIPTION_PENDING,
            OrderStatus.PRESCRIPTION_APPROVED,
            OrderStatus.PAYMENT_PENDING,
            OrderStatus.PAYMENT_FAILED,
            OrderStatus.PAID);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;

    @Autowired(required = false)
    private DomainEventPublisher domainEventPublisher;

    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OrderResponse> getAllOrdersForAdmin(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 1000);
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(safePage, safeSize))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toResponse(order);
    }

    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toResponse(order);
    }

    @Transactional
    public CartResponse reorder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItem> previousItems = orderItemRepository.findByOrderId(order.getId());
        if (previousItems.isEmpty()) {
            throw new InvalidOrderStateException("Cannot reorder an order without items");
        }

        cartService.clearCart(userId);

        for (OrderItem item : previousItems) {
            AddToCartRequest request = new AddToCartRequest();
            request.setMedicineId(item.getMedicineId());
            request.setQuantity(item.getQuantity());
            request.setSubstituteAllowed(false);
            cartService.addToCart(userId, request);
        }

        return cartService.getCart(userId);
    }

    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId, String reason, String bearerToken) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!CUSTOMER_CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new InvalidOrderStateException("Order cannot be cancelled in status: " + order.getStatus());
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CUSTOMER_CANCELLED);
        order.setCancellationReason(
                reason == null || reason.isBlank()
                        ? "Cancelled by customer"
                        : reason.trim());

        Order savedOrder = orderRepository.save(order);
        publishOrderStatusChangedEvent(savedOrder, previousStatus, "CUSTOMER", savedOrder.getCancellationReason());

        if (savedOrder.getPrescriptionId() != null) {
            catalogClient.cancelPrescription(savedOrder.getPrescriptionId(), bearerToken);
        }

        return toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus, String bearerToken) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus currentStatus = order.getStatus();
        if (!isTransitionAllowed(currentStatus, newStatus)) {
            throw new InvalidOrderStateException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        order.setStatus(newStatus);

        if (newStatus == OrderStatus.ADMIN_CANCELLED
                && (order.getCancellationReason() == null || order.getCancellationReason().isBlank())) {
            order.setCancellationReason("Cancelled by admin");
        }

        Order savedOrder = orderRepository.save(order);
        if (currentStatus != savedOrder.getStatus()) {
            publishOrderStatusChangedEvent(savedOrder, currentStatus, "ADMIN", savedOrder.getCancellationReason());
        }

        if (newStatus == OrderStatus.ADMIN_CANCELLED && savedOrder.getPrescriptionId() != null) {
            catalogClient.cancelPrescription(savedOrder.getPrescriptionId(), bearerToken);
        }

        return toResponse(savedOrder);
    }

    @Transactional
    public void handlePrescriptionReviewed(Long prescriptionId, String newPrescriptionStatus) {
        if (prescriptionId == null || newPrescriptionStatus == null) {
            return;
        }

        Order order = orderRepository.findByPrescriptionId(prescriptionId).orElse(null);
        if (order == null) {
            return;
        }

        if (order.getStatus() != OrderStatus.PRESCRIPTION_PENDING) {
            return;
        }

        OrderStatus targetStatus = switch (newPrescriptionStatus.trim().toUpperCase()) {
            case "APPROVED" -> OrderStatus.PRESCRIPTION_APPROVED;
            case "REJECTED" -> OrderStatus.PRESCRIPTION_REJECTED;
            default -> null;
        };

        if (targetStatus == null) {
            return;
        }

        OrderStatus previous = order.getStatus();
        order.setStatus(targetStatus);
        Order saved = orderRepository.save(order);
        publishOrderStatusChangedEvent(saved, previous, "ADMIN", null);
    }

    private boolean isTransitionAllowed(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return true;
        }

        return switch (current) {
            case DRAFT -> Set.of(OrderStatus.CHECKOUT_STARTED, OrderStatus.CUSTOMER_CANCELLED,
                    OrderStatus.ADMIN_CANCELLED).contains(next);

            case CHECKOUT_STARTED -> Set.of(OrderStatus.PRESCRIPTION_PENDING, OrderStatus.PAYMENT_PENDING,
                    OrderStatus.CUSTOMER_CANCELLED, OrderStatus.ADMIN_CANCELLED).contains(next);

            case PRESCRIPTION_PENDING -> Set.of(OrderStatus.PRESCRIPTION_APPROVED, OrderStatus.PRESCRIPTION_REJECTED,
                    OrderStatus.CUSTOMER_CANCELLED, OrderStatus.ADMIN_CANCELLED).contains(next);

            case PRESCRIPTION_APPROVED -> Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CUSTOMER_CANCELLED,
                    OrderStatus.ADMIN_CANCELLED).contains(next);

            case PRESCRIPTION_REJECTED -> Set.of(OrderStatus.CUSTOMER_CANCELLED, OrderStatus.ADMIN_CANCELLED)
                    .contains(next);

            case PAYMENT_PENDING -> Set.of(OrderStatus.PAID, OrderStatus.PAYMENT_FAILED,
                    OrderStatus.CUSTOMER_CANCELLED, OrderStatus.ADMIN_CANCELLED).contains(next);

            case PAYMENT_FAILED -> Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CUSTOMER_CANCELLED,
                    OrderStatus.ADMIN_CANCELLED).contains(next);

            case PAID -> Set.of(OrderStatus.PACKED, OrderStatus.REFUND_INITIATED,
                    OrderStatus.ADMIN_CANCELLED).contains(next);

            case PACKED -> Set.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.ADMIN_CANCELLED).contains(next);

            case OUT_FOR_DELIVERY -> Set.of(OrderStatus.DELIVERED).contains(next);

            case DELIVERED -> Set.of(OrderStatus.RETURN_REQUESTED).contains(next);

            case RETURN_REQUESTED -> Set.of(OrderStatus.REFUND_INITIATED).contains(next);

            case REFUND_INITIATED -> Set.of(OrderStatus.REFUND_COMPLETED).contains(next);

            case CUSTOMER_CANCELLED, ADMIN_CANCELLED, REFUND_COMPLETED -> false;
        };
    }

    private void publishOrderStatusChangedEvent(
            Order order,
            OrderStatus previousStatus,
            String changedBy,
            String reason) {

        if (domainEventPublisher == null) {
            return;
        }

        OrderStatusChangedEvent event = OrderStatusChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(OffsetDateTime.now().toString())
                .orderId(order.getId())
                .userId(order.getUserId())
                .previousStatus(previousStatus.name())
                .newStatus(order.getStatus().name())
                .changedBy(changedBy)
                .reason(reason)
                .build();

        domainEventPublisher.publishAfterCommit(PharmacyEventRoutingKeys.ORDER_STATUS_CHANGED, event);
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setDeliverySlot(order.getDeliverySlot());
        response.setPincode(order.getDeliveryPincode());
        response.setTotalAmount(order.getTotalAmount());
        response.setFinalAmount(order.getFinalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setPrescriptionId(order.getPrescriptionId());

        if (order.getPayment() != null) {
            response.setPaymentMethod(order.getPayment().getMethod());
            response.setPaymentId(order.getPayment().getTransactionId());
        }
        // userName / userEmail are populated by the admin-service via its own
        // cross-service call (with the real admin JWT). Not populated here.

        var items = orderItemRepository.findByOrderId(order.getId())
                .stream()
                .map(item -> {
                    OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
                    itemResponse.setId(item.getId());
                    itemResponse.setMedicineId(item.getMedicineId());
                    itemResponse.setMedicineName(item.getMedicineName());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setUnitPrice(item.getUnitPrice());
                    itemResponse.setTotalPrice(item.getSubtotal());
                    return itemResponse;
                })
                .toList();

        response.setItems(items);
        return response;
    }
}