package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.request.OrderStatusUpdateDto;
import com.pharmacy.admin.dto.response.OrderResponseDto;
import com.pharmacy.admin.entity.Order;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.exception.InvalidStatusTransitionException;
import com.pharmacy.admin.exception.ResourceNotFoundException;
import com.pharmacy.admin.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminOrderService Tests")
class AdminOrderServiceTest {

    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private AdminOrderService orderService;

    private Order samplePaidOrder;
    private Order samplePackedOrder;
    private Order samplePrescriptionPendingOrder;

    @BeforeEach
    void setUp() {
        samplePaidOrder = Order.builder()
                .id(1L).userId(10L).userEmail("user@test.com")
                .status(OrderStatus.PAID)
                .totalAmount(500.0)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        samplePackedOrder = Order.builder()
                .id(2L).userId(10L).userEmail("user@test.com")
                .status(OrderStatus.PACKED)
                .totalAmount(300.0)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        samplePrescriptionPendingOrder = Order.builder()
                .id(3L).userId(10L).userEmail("user@test.com")
                .status(OrderStatus.PRESCRIPTION_PENDING)
                .totalAmount(800.0)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateOrderStatus() — valid transitions")
    class ValidTransitionTests {

        @Test
        @DisplayName("PAID → PACKED should succeed")
        void updateStatus_paidToPacked_succeeds() {
            OrderStatusUpdateDto dto = new OrderStatusUpdateDto();
            dto.setStatus("PACKED");

            when(orderRepository.findById(1L)).thenReturn(Optional.of(samplePaidOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            OrderResponseDto result = orderService.updateOrderStatus(1L, dto);

            assertThat(result.getStatus()).isEqualTo("PACKED");
            verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.PACKED));
        }

        @Test
        @DisplayName("PACKED → OUT_FOR_DELIVERY should succeed")
        void updateStatus_packedToOutForDelivery_succeeds() {
            OrderStatusUpdateDto dto = new OrderStatusUpdateDto();
            dto.setStatus("OUT_FOR_DELIVERY");

            when(orderRepository.findById(2L)).thenReturn(Optional.of(samplePackedOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            OrderResponseDto result = orderService.updateOrderStatus(2L, dto);

            assertThat(result.getStatus()).isEqualTo("OUT_FOR_DELIVERY");
        }

        @Test
        @DisplayName("Admin note should be saved when provided")
        void updateStatus_withAdminNote_savesNote() {
            OrderStatusUpdateDto dto = new OrderStatusUpdateDto();
            dto.setStatus("PACKED");
            dto.setAdminNote("Packed by warehouse team B");

            when(orderRepository.findById(1L)).thenReturn(Optional.of(samplePaidOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            orderService.updateOrderStatus(1L, dto);

            verify(orderRepository).save(argThat(
                    o -> "Packed by warehouse team B".equals(o.getAdminNote())));
        }

        @Test
        @DisplayName("Delivered order should have deliveredAt timestamp set")
        void updateStatus_toDelivered_setsDeliveredAt() {
            Order outForDelivery = Order.builder()
                    .id(4L).status(OrderStatus.OUT_FOR_DELIVERY)
                    .items(new ArrayList<>()).createdAt(LocalDateTime.now()).build();

            OrderStatusUpdateDto dto = new OrderStatusUpdateDto();
            dto.setStatus("DELIVERED");

            when(orderRepository.findById(4L)).thenReturn(Optional.of(outForDelivery));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            orderService.updateOrderStatus(4L, dto);

            verify(orderRepository).save(argThat(o -> o.getDeliveredAt() != null));
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateOrderStatus() — invalid transitions")
    class InvalidTransitionTests {

        @ParameterizedTest(name = "PAID → {0} should fail")
        @CsvSource({"DELIVERED", "OUT_FOR_DELIVERY", "DRAFT_CART", "PAID"})
        @DisplayName("Invalid transitions from PAID should throw exception")
        void updateStatus_invalidFromPaid_throwsException(String targetStatus) {
            OrderStatusUpdateDto dto = new OrderStatusUpdateDto();
            dto.setStatus(targetStatus);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(samplePaidOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, dto))
                    .isInstanceOf(InvalidStatusTransitionException.class)
                    .hasMessageContaining("PAID");
        }

        @Test
        @DisplayName("Should throw BadRequestException for unknown status string")
        void updateStatus_unknownStatus_throwsException() {
            OrderStatusUpdateDto dto = new OrderStatusUpdateDto();
            dto.setStatus("FLYING_THROUGH_AIR");

            when(orderRepository.findById(1L)).thenReturn(Optional.of(samplePaidOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid order status");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order id does not exist")
        void updateStatus_orderNotFound_throwsException() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            OrderStatusUpdateDto dto = new OrderStatusUpdateDto();
            dto.setStatus("PACKED");

            assertThatThrownBy(() -> orderService.updateOrderStatus(999L, dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("approvePrescription()")
    class ApprovePrescriptionTests {

        @Test
        @DisplayName("Should approve prescription when order is PRESCRIPTION_PENDING")
        void approvePrescription_validState_succeeds() {
            when(orderRepository.findById(3L))
                    .thenReturn(Optional.of(samplePrescriptionPendingOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            OrderResponseDto result = orderService.approvePrescription(3L, null);

            assertThat(result.getStatus()).isEqualTo("PRESCRIPTION_APPROVED");
        }

        @Test
        @DisplayName("Should throw BadRequestException when order is not PRESCRIPTION_PENDING")
        void approvePrescription_wrongState_throwsException() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(samplePaidOrder));

            assertThatThrownBy(() -> orderService.approvePrescription(1L, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("PRESCRIPTION_PENDING");
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("rejectPrescription()")
    class RejectPrescriptionTests {

        @Test
        @DisplayName("Should reject with reason and save to adminNote")
        void rejectPrescription_withReason_succeeds() {
            when(orderRepository.findById(3L))
                    .thenReturn(Optional.of(samplePrescriptionPendingOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            OrderResponseDto result = orderService.rejectPrescription(
                    3L, "Prescription is illegible");

            assertThat(result.getStatus()).isEqualTo("PRESCRIPTION_REJECTED");
        }

        @Test
        @DisplayName("Should throw BadRequestException when rejection reason is empty")
        void rejectPrescription_noReason_throwsException() {
            when(orderRepository.findById(3L))
                    .thenReturn(Optional.of(samplePrescriptionPendingOrder));

            assertThatThrownBy(() -> orderService.rejectPrescription(3L, ""))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("reason");
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel a PAID order")
        void cancelOrder_paidOrder_succeeds() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(samplePaidOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            OrderResponseDto result = orderService.cancelOrder(1L, "Out of stock");

            assertThat(result.getStatus()).isEqualTo("ADMIN_CANCELLED");
        }

        @Test
        @DisplayName("Should throw BadRequestException when order is already DELIVERED")
        void cancelOrder_deliveredOrder_throwsException() {
            Order delivered = Order.builder().id(5L)
                    .status(OrderStatus.DELIVERED).items(new ArrayList<>())
                    .createdAt(LocalDateTime.now()).build();

            when(orderRepository.findById(5L)).thenReturn(Optional.of(delivered));

            assertThatThrownBy(() -> orderService.cancelOrder(5L, "reason"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot cancel");
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getAllOrders()")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Should return paginated list of orders")
        void getAllOrders_returnsPagedList() {
            List<Order> orders = List.of(samplePaidOrder, samplePackedOrder);
            when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(orders));

            List<OrderResponseDto> result = orderService.getAllOrders(0, 20);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStatus()).isEqualTo("PAID");
            assertThat(result.get(1).getStatus()).isEqualTo("PACKED");
        }
    }
}
