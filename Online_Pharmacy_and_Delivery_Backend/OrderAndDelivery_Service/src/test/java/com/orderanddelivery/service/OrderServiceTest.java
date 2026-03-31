package com.orderanddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.orderanddelivery.entities.Order;
import com.orderanddelivery.entities.OrderItem;
import com.orderanddelivery.enums.OrderStatus;
import com.orderanddelivery.exception.InvalidOrderStateException;
import com.orderanddelivery.exception.ResourceNotFoundException;
import com.orderanddelivery.repository.OrderItemRepository;
import com.orderanddelivery.repository.OrderRepository;
import com.orderanddelivery.responseDTO.CartResponse;
import com.orderanddelivery.responseDTO.OrderResponse;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void cancelOrder_paymentPending_succeeds() {
        Order order = orderWithStatus(1L, OrderStatus.PAYMENT_PENDING);

        when(orderRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of());

        OrderResponse response = orderService.cancelOrder(10L, 1L, "Changed my mind");

        assertEquals(OrderStatus.CUSTOMER_CANCELLED, response.getStatus());
        assertEquals("Changed my mind", order.getCancellationReason());
    }

    @Test
    void cancelOrder_delivered_throwsInvalidState() {
        Order order = orderWithStatus(1L, OrderStatus.DELIVERED);
        when(orderRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.cancelOrder(10L, 1L, "Too late"));
    }

    @Test
    void cancelOrder_wrongUser_throwsResourceNotFound() {
        when(orderRepository.findByIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.cancelOrder(99L, 1L, "Not owner"));
    }

    @Test
    void reorder_success_rebuildsCartFromOrderItems() {
        Order order = orderWithStatus(5L, OrderStatus.DELIVERED);
        when(orderRepository.findByIdAndUserId(5L, 10L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(5L)).thenReturn(List.of(
                OrderItem.builder().medicineId(101L).quantity(2).build(),
                OrderItem.builder().medicineId(102L).quantity(1).build()));

        CartResponse expectedCart = new CartResponse();
        expectedCart.setTotalItems(3);
        when(cartService.getCart(10L)).thenReturn(expectedCart);

        CartResponse response = orderService.reorder(10L, 5L);

        assertEquals(3, response.getTotalItems());
        verify(cartService).clearCart(10L);
        verify(cartService, times(2)).addToCart(any(Long.class), any());
    }

    @Test
    void reorder_missingOrder_throwsResourceNotFound() {
        when(orderRepository.findByIdAndUserId(5L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.reorder(10L, 5L));
    }

    @Test
    void updateStatus_validTransition_paidToPacked() {
        Order order = orderWithStatus(2L, OrderStatus.PAID);

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.findByOrderId(2L)).thenReturn(List.of());

        OrderResponse response = orderService.updateStatus(2L, OrderStatus.PACKED);

        assertEquals(OrderStatus.PACKED, response.getStatus());
    }

    @Test
    void updateStatus_invalidTransition_packedToDelivered_throws() {
        Order order = orderWithStatus(3L, OrderStatus.PACKED);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.updateStatus(3L, OrderStatus.DELIVERED));
    }

    @Test
    void updateStatus_terminalState_customerCancelled_throws() {
        Order order = orderWithStatus(4L, OrderStatus.CUSTOMER_CANCELLED);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.updateStatus(4L, OrderStatus.PACKED));
    }

    private Order orderWithStatus(Long id, OrderStatus status) {
        return Order.builder()
                .id(id)
                .userId(10L)
                .status(status)
                .deliveryAddress("FC Road, Pune")
                .deliverySlot("9AM-12PM")
                .totalAmount(new BigDecimal("250.00"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("250.00"))
                .build();
    }
}