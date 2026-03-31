package com.orderanddelivery.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderanddelivery.entities.Order;
import com.orderanddelivery.entities.Payment;
import com.orderanddelivery.enums.OrderStatus;
import com.orderanddelivery.enums.PaymentStatus;
import com.orderanddelivery.exception.InvalidOrderStateException;
import com.orderanddelivery.exception.ResourceNotFoundException;
import com.orderanddelivery.messaging.DomainEventPublisher;
import com.orderanddelivery.messaging.PharmacyEventRoutingKeys;
import com.orderanddelivery.messaging.events.OrderStatusChangedEvent;
import com.orderanddelivery.messaging.events.PaymentStatusEvent;
import com.orderanddelivery.repository.OrderRepository;
import com.orderanddelivery.repository.PaymentRepository;
import com.orderanddelivery.requestDTO.PaymentRequest;
import com.orderanddelivery.responseDTO.PaymentResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Autowired(required = false)
    private DomainEventPublisher domainEventPublisher;

    @Transactional
    public PaymentResponse initiatePayment(Long userId, PaymentRequest request) {

        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Optional<Payment> existingPaymentOptional = paymentRepository.findByOrderIdAndOrderUserId(
                request.getOrderId(),
                userId);

        if (existingPaymentOptional.isPresent() && existingPaymentOptional.get().getStatus() == PaymentStatus.SUCCESS) {
            return toResponse(existingPaymentOptional.get(), "Payment is already completed for this order.");
        }

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING
                && order.getStatus() != OrderStatus.PRESCRIPTION_APPROVED
                && order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            throw new InvalidOrderStateException(
                    "Order is not eligible for payment. Current status: " + order.getStatus());
        }

        OrderStatus previousOrderStatus = order.getStatus();
        boolean paymentSuccess = !"FAIL_TEST".equalsIgnoreCase(request.getMethod());

        Payment payment = existingPaymentOptional.orElseGet(() -> Payment.builder().order(order).build());
        payment.setMethod(request.getMethod().trim().toUpperCase(Locale.ROOT));
        payment.setAmount(order.getFinalAmount());
        payment.setTransactionId(resolveTransactionId(request.getTransactionRef()));
        payment.setCompletedAt(LocalDateTime.now());

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setFailureReason(null);
            order.setStatus(OrderStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Simulated payment failure");
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        orderRepository.save(order);
        Payment savedPayment = paymentRepository.save(payment);

        publishPaymentEvent(savedPayment, userId);
        if (previousOrderStatus != order.getStatus()) {
            publishOrderStatusChangedEvent(order, previousOrderStatus, "SYSTEM_PAYMENT", savedPayment.getFailureReason());
        }

        String message = paymentSuccess
                ? "Payment successful. Your order is confirmed."
                : "Payment failed. Please retry.";

        return toResponse(savedPayment, message);
    }

    private String resolveTransactionId(String transactionRef) {
        if (transactionRef != null && !transactionRef.isBlank()) {
            return transactionRef.trim();
        }
        return UUID.randomUUID().toString();
    }

    private void publishPaymentEvent(Payment payment, Long userId) {
        if (domainEventPublisher == null) {
            return;
        }

        PaymentStatusEvent event = PaymentStatusEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(OffsetDateTime.now().toString())
                .orderId(payment.getOrder().getId())
                .userId(userId)
                .paymentId(payment.getId())
                .paymentStatus(payment.getStatus().name())
                .paymentMethod(payment.getMethod())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .build();

        String routingKey = payment.getStatus() == PaymentStatus.SUCCESS
                ? PharmacyEventRoutingKeys.PAYMENT_SUCCEEDED
                : PharmacyEventRoutingKeys.PAYMENT_FAILED;

        domainEventPublisher.publishAfterCommit(routingKey, event);
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

    private PaymentResponse toResponse(Payment payment, String message) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setStatus(payment.getStatus());
        response.setMethod(payment.getMethod());
        response.setTransactionId(payment.getTransactionId());
        response.setAmount(payment.getAmount());
        response.setMessage(message);
        return response;
    }
}
