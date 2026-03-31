package com.orderanddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.orderanddelivery.entities.Order;
import com.orderanddelivery.entities.Payment;
import com.orderanddelivery.enums.OrderStatus;
import com.orderanddelivery.enums.PaymentStatus;
import com.orderanddelivery.exception.InvalidOrderStateException;
import com.orderanddelivery.exception.ResourceNotFoundException;
import com.orderanddelivery.repository.OrderRepository;
import com.orderanddelivery.repository.PaymentRepository;
import com.orderanddelivery.requestDTO.PaymentRequest;
import com.orderanddelivery.responseDTO.PaymentResponse;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void initiatePayment_orderNotFound_throwsResourceNotFound() {
        PaymentRequest request = paymentRequest(1L, "UPI", "TXN-1");
        when(orderRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.initiatePayment(10L, request));
    }

    @Test
    void initiatePayment_wrongOrderStatus_throwsInvalidState() {
        PaymentRequest request = paymentRequest(1L, "UPI", "TXN-1");
        Order order = orderWithStatus(OrderStatus.PACKED);

        when(orderRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderIdAndOrderUserId(1L, 10L)).thenReturn(Optional.empty());

        InvalidOrderStateException ex = assertThrows(InvalidOrderStateException.class,
                () -> paymentService.initiatePayment(10L, request));

        assertTrue(ex.getMessage().contains("PACKED"));
    }

    @Test
    void initiatePayment_success_setsOrderToPaidAndReturnsSuccess() {
        PaymentRequest request = paymentRequest(1L, "UPI", "TXN-OK");
        Order order = orderWithStatus(OrderStatus.PAYMENT_PENDING);

        when(orderRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderIdAndOrderUserId(1L, 10L)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(11L);
            return payment;
        });

        PaymentResponse response = paymentService.initiatePayment(10L, request);

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void initiatePayment_failTestMethod_setsOrderToPaymentFailed() {
        PaymentRequest request = paymentRequest(1L, "FAIL_TEST", "TXN-FAIL");
        Order order = orderWithStatus(OrderStatus.PAYMENT_PENDING);

        when(orderRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderIdAndOrderUserId(1L, 10L)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.initiatePayment(10L, request);

        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
    }

    @Test
    void initiatePayment_alreadyPaidOrder_returnsExistingPayment() {
        PaymentRequest request = paymentRequest(1L, "UPI", "TXN-1");
        Order order = orderWithStatus(OrderStatus.PACKED);
        Payment existing = Payment.builder()
                .id(50L)
                .order(order)
                .status(PaymentStatus.SUCCESS)
                .method("UPI")
                .transactionId("TXN-DONE")
                .amount(new BigDecimal("250.00"))
                .build();

        when(orderRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderIdAndOrderUserId(1L, 10L)).thenReturn(Optional.of(existing));

        PaymentResponse response = paymentService.initiatePayment(10L, request);

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("TXN-DONE", response.getTransactionId());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    private PaymentRequest paymentRequest(Long orderId, String method, String ref) {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setMethod(method);
        request.setTransactionRef(ref);
        return request;
    }

    private Order orderWithStatus(OrderStatus status) {
        return Order.builder()
                .id(1L)
                .userId(10L)
                .status(status)
                .totalAmount(new BigDecimal("250.00"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("250.00"))
                .deliveryAddress("FC Road, Pune")
                .deliveryPincode("411001")
                .deliveryPhone("9999999999")
                .deliverySlot("9AM-12PM")
                .build();
    }
}
