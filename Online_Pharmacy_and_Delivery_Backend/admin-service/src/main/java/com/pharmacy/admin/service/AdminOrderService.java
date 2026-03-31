package com.pharmacy.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.admin.dto.request.OrderStatusUpdateDto;
import com.pharmacy.admin.dto.response.OrderItemResponseDto;
import com.pharmacy.admin.dto.response.OrderResponseDto;
import com.pharmacy.admin.entity.Order;
import com.pharmacy.admin.entity.OrderItem;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.exception.InvalidStatusTransitionException;
import com.pharmacy.admin.exception.ResourceNotFoundException;
import com.pharmacy.admin.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderService.class);

    private static final Set<OrderStatus> NON_CANCELLABLE_BY_ADMIN = Set.of(
            OrderStatus.DELIVERED,
            OrderStatus.ADMIN_CANCELLED,
            OrderStatus.CUSTOMER_CANCELLED,
            OrderStatus.REFUND_COMPLETED);

    private final OrderRepository orderRepository;

    public List<OrderResponseDto> getAllOrders(int page, int size) {
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<OrderResponseDto> getOrdersByStatus(String status) {
        OrderStatus parsedStatus = parseStatus(status);
        return orderRepository.findByStatusOrderByCreatedAtDesc(parsedStatus)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public OrderResponseDto getOrderById(Long id) {
        return mapToDto(findOrderOrThrow(id));
    }

    public List<OrderResponseDto> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<OrderResponseDto> getActiveOrders() {
        return orderRepository.findActiveOrders().stream().map(this::mapToDto).toList();
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateDto dto) {
        Order order = findOrderOrThrow(orderId);

        OrderStatus currentStatus = order.getStatus();
        OrderStatus nextStatus = parseStatus(dto.getStatus());

        validateTransition(currentStatus, nextStatus);

        order.setStatus(nextStatus);

        String adminNote = normalizeOptional(dto.getAdminNote());
        if (adminNote != null) {
            order.setAdminNote(adminNote);
        }

        if (nextStatus == OrderStatus.DELIVERED && order.getDeliveredAt() == null) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        Order saved = orderRepository.save(order);
        log.info("Order id={} status changed from {} to {}", orderId, currentStatus, nextStatus);
        return mapToDto(saved);
    }

    @Transactional
    public OrderResponseDto approvePrescription(Long orderId, String adminNote) {
        Order order = findOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.PRESCRIPTION_PENDING) {
            throw new BadRequestException("Order is not in PRESCRIPTION_PENDING state. Current: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PRESCRIPTION_APPROVED);

        String normalizedNote = normalizeOptional(adminNote);
        if (normalizedNote != null) {
            order.setAdminNote(normalizedNote);
        }

        log.info("Prescription approved for order id={}", orderId);
        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDto rejectPrescription(Long orderId, String reason) {
        Order order = findOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.PRESCRIPTION_PENDING) {
            throw new BadRequestException("Order is not in PRESCRIPTION_PENDING state. Current: " + order.getStatus());
        }

        String normalizedReason = normalizeRequired(reason, "Rejection reason is required");

        order.setStatus(OrderStatus.PRESCRIPTION_REJECTED);
        order.setAdminNote(normalizedReason);

        log.info("Prescription rejected for order id={}", orderId);
        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, String reason) {
        Order order = findOrderOrThrow(orderId);

        if (NON_CANCELLABLE_BY_ADMIN.contains(order.getStatus())) {
            throw new BadRequestException("Cannot cancel an order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.ADMIN_CANCELLED);
        order.setAdminNote(normalizeOptional(reason) != null ? normalizeOptional(reason) : "Cancelled by admin");

        log.info("Order id={} cancelled by admin", orderId);
        return mapToDto(orderRepository.save(order));
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PAID -> next == OrderStatus.PACKED || next == OrderStatus.ADMIN_CANCELLED;
            case PACKED -> next == OrderStatus.OUT_FOR_DELIVERY || next == OrderStatus.ADMIN_CANCELLED;
            case OUT_FOR_DELIVERY -> next == OrderStatus.DELIVERED || next == OrderStatus.ADMIN_CANCELLED;
            case PRESCRIPTION_PENDING -> next == OrderStatus.PRESCRIPTION_APPROVED || next == OrderStatus.PRESCRIPTION_REJECTED;
            case PRESCRIPTION_APPROVED -> next == OrderStatus.PAYMENT_PENDING || next == OrderStatus.ADMIN_CANCELLED;
            case PAYMENT_PENDING -> next == OrderStatus.PAID || next == OrderStatus.PAYMENT_FAILED || next == OrderStatus.ADMIN_CANCELLED;
            case DELIVERED -> next == OrderStatus.RETURN_REQUESTED;
            case RETURN_REQUESTED -> next == OrderStatus.REFUND_INITIATED;
            case REFUND_INITIATED -> next == OrderStatus.REFUND_COMPLETED;
            default -> false;
        };

        if (!valid) {
            throw new InvalidStatusTransitionException(current.name(), next.name());
        }
    }

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new BadRequestException("Order status is required");
        }

        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid order status: " + status);
        }
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public OrderResponseDto mapToDto(Order order) {
        List<OrderItemResponseDto> itemDtos = order.getItems() == null
                ? List.of()
                : order.getItems().stream().map(this::mapItemToDto).toList();

        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .userName(order.getUserName())
                .status(order.getStatus() == null ? null : order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .deliveryCharge(order.getDeliveryCharge())
                .items(itemDtos)
                .deliveryAddress(order.getDeliveryAddress())
                .pincode(order.getPincode())
                .deliverySlot(order.getDeliverySlot())
                .prescriptionId(order.getPrescriptionId())
                .adminNote(order.getAdminNote())
                .paymentMethod(order.getPaymentMethod())
                .paymentId(order.getPaymentId())
                .createdAt(order.getCreatedAt() == null ? null : order.getCreatedAt().toString())
                .updatedAt(order.getUpdatedAt() == null ? null : order.getUpdatedAt().toString())
                .deliveredAt(order.getDeliveredAt() == null ? null : order.getDeliveredAt().toString())
                .build();
    }

    private OrderItemResponseDto mapItemToDto(OrderItem item) {
        return OrderItemResponseDto.builder()
                .id(item.getId())
                .medicineId(item.getMedicine() == null ? null : item.getMedicine().getId())
                .medicineName(item.getMedicine() == null ? null : item.getMedicine().getName())
                .medicineStrength(item.getMedicine() == null ? null : item.getMedicine().getStrength())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
