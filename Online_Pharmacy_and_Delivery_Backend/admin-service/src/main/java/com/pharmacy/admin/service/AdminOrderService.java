package com.pharmacy.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.admin.dto.request.OrderStatusUpdateDto;
import com.pharmacy.admin.dto.response.OrderItemResponseDto;
import com.pharmacy.admin.dto.response.OrderResponseDto;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.integration.CrossServiceAnalyticsClient;
import com.pharmacy.admin.integration.RemoteOrderResponse;
import com.pharmacy.admin.integration.RemoteOrderResponse.RemoteOrderItemResponse;
import com.pharmacy.admin.integration.RemoteUserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderService.class);

    private final CrossServiceAnalyticsClient crossServiceClient;

    public List<OrderResponseDto> getAllOrders(int page, int size) {
        Optional<List<RemoteOrderResponse>> remoteOrders = crossServiceClient.fetchAdminOrders();
        if (remoteOrders.isPresent()) {
            Map<Long, RemoteUserResponse> userCache = buildUserCache(remoteOrders.get());
            return remoteOrders.get().stream()
                    .map(o -> mapRemoteToDto(o, userCache))
                    .toList();
        }

        log.warn("Falling back to local orders due to remote service unavailable");
        return List.of();
    }

    public List<OrderResponseDto> getOrdersByStatus(String status) {
        Optional<List<RemoteOrderResponse>> remoteOrders = crossServiceClient.fetchAdminOrders();
        if (remoteOrders.isPresent()) {
            List<RemoteOrderResponse> matched = remoteOrders.get().stream()
                    .filter(o -> status == null || status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(o.getStatus()))
                    .toList();
            Map<Long, RemoteUserResponse> userCache = buildUserCache(matched);
            return matched.stream()
                    .map(o -> mapRemoteToDto(o, userCache))
                    .toList();
        }
        return List.of();
    }

    public OrderResponseDto getOrderById(Long id) {
        Optional<RemoteOrderResponse> remoteOrder = crossServiceClient.fetchAdminOrderById(id);
        if (remoteOrder.isPresent()) {
            Map<Long, RemoteUserResponse> userCache = buildUserCache(List.of(remoteOrder.get()));
            return mapRemoteToDto(remoteOrder.get(), userCache);
        }

        throw new BadRequestException("Order not found: " + id);
    }

    public List<OrderResponseDto> getOrdersByUser(Long userId) {
        Optional<List<RemoteOrderResponse>> remoteOrders = crossServiceClient.fetchAdminOrders();
        if (remoteOrders.isPresent()) {
            List<RemoteOrderResponse> matched = remoteOrders.get().stream()
                    .filter(o -> userId.equals(o.getUserId()))
                    .toList();
            Map<Long, RemoteUserResponse> userCache = buildUserCache(matched);
            return matched.stream()
                    .map(o -> mapRemoteToDto(o, userCache))
                    .toList();
        }
        return List.of();
    }

    public List<OrderResponseDto> getActiveOrders() {
        Optional<List<RemoteOrderResponse>> remoteOrders = crossServiceClient.fetchAdminOrders();
        if (remoteOrders.isPresent()) {
            List<String> activeStatuses = List.of("PAID", "PACKED", "OUT_FOR_DELIVERY", "PRESCRIPTION_PENDING", "PRESCRIPTION_APPROVED", "PAYMENT_PENDING");
            List<RemoteOrderResponse> matched = remoteOrders.get().stream()
                    .filter(o -> o.getStatus() != null && activeStatuses.contains(o.getStatus().toUpperCase()))
                    .toList();
            Map<Long, RemoteUserResponse> userCache = buildUserCache(matched);
            return matched.stream()
                    .map(o -> mapRemoteToDto(o, userCache))
                    .toList();
        }
        return List.of();
    }

    private Map<Long, RemoteUserResponse> buildUserCache(List<RemoteOrderResponse> orders) {
        Map<Long, RemoteUserResponse> cache = new HashMap<>();
        for (RemoteOrderResponse order : orders) {
            Long userId = order.getUserId();
            if (userId == null || cache.containsKey(userId)) {
                continue;
            }
            crossServiceClient.fetchUserById(userId).ifPresent(user -> cache.put(userId, user));
        }
        return cache;
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateDto dto) {
        Optional<RemoteOrderResponse> updated = crossServiceClient.updateOrderStatus(orderId, dto.getStatus());
        if (updated.isPresent()) {
            log.info("Order id={} status updated to {} via remote service", orderId, dto.getStatus());
            Map<Long, RemoteUserResponse> userCache = buildUserCache(List.of(updated.get()));
            return mapRemoteToDto(updated.get(), userCache);
        }

        log.error("Failed to update order status via remote service, orderId={}", orderId);
        throw new BadRequestException("Failed to update order status. Remote service may be unavailable.");
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, String reason) {
        Optional<RemoteOrderResponse> updated = crossServiceClient.cancelOrder(orderId, reason);
        if (updated.isPresent()) {
            log.info("Order id={} cancelled via remote service", orderId);
            Map<Long, RemoteUserResponse> userCache = buildUserCache(List.of(updated.get()));
            return mapRemoteToDto(updated.get(), userCache);
        }

        log.error("Failed to cancel order via remote service, orderId={}", orderId);
        throw new BadRequestException("Failed to cancel order. Remote service may be unavailable.");
    }

    private OrderResponseDto mapRemoteToDto(RemoteOrderResponse remote, Map<Long, RemoteUserResponse> userCache) {
        List<OrderItemResponseDto> items = null;
        if (remote.getItems() != null) {
            items = remote.getItems().stream()
                    .map(this::mapRemoteItemToDto)
                    .toList();
        }

        RemoteUserResponse user = remote.getUserId() == null ? null : userCache.get(remote.getUserId());
        String userEmail = remote.getUserEmail() != null ? remote.getUserEmail()
                : (user != null ? user.getEmail() : null);
        String userName = remote.getUserName() != null ? remote.getUserName()
                : (user != null ? (user.getName() != null ? user.getName() : user.getUsername()) : null);

        return OrderResponseDto.builder()
                .id(remote.getId())
                .userId(remote.getUserId())
                .userEmail(userEmail)
                .userName(userName)
                .status(remote.getStatus())
                .totalAmount(remote.getTotalAmount() != null ? remote.getTotalAmount().doubleValue() : 0.0)
                .discountAmount(remote.getDiscountAmount() != null ? remote.getDiscountAmount().doubleValue() : 0.0)
                .taxAmount(remote.getTaxAmount() != null ? remote.getTaxAmount().doubleValue() : 0.0)
                .deliveryCharge(remote.getDeliveryCharge() != null ? remote.getDeliveryCharge().doubleValue() : 0.0)
                .deliveryAddress(remote.getDeliveryAddress())
                .pincode(remote.getPincode())
                .deliverySlot(remote.getDeliverySlot())
                .prescriptionId(remote.getPrescriptionId())
                .adminNote(remote.getAdminNote())
                .paymentMethod(remote.getPaymentMethod())
                .paymentId(remote.getPaymentId())
                .createdAt(remote.getCreatedAt() == null ? null : remote.getCreatedAt().toString())
                .updatedAt(remote.getUpdatedAt() == null ? null : remote.getUpdatedAt().toString())
                .deliveredAt(remote.getDeliveredAt() == null ? null : remote.getDeliveredAt().toString())
                .items(items)
                .build();
    }

    private OrderItemResponseDto mapRemoteItemToDto(RemoteOrderItemResponse item) {
        return OrderItemResponseDto.builder()
                .id(item.getId())
                .medicineId(item.getMedicineId())
                .medicineName(item.getMedicineName())
                .medicineStrength(item.getMedicineStrength())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0.0)
                .totalPrice(item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : 0.0)
                .build();
    }
}