package com.orderanddelivery.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.orderanddelivery.entities.CartItem;
import com.orderanddelivery.entities.Order;
import com.orderanddelivery.entities.OrderItem;
import com.orderanddelivery.entities.Payment;
import com.orderanddelivery.enums.OrderStatus;
import com.orderanddelivery.enums.PaymentStatus;
import com.orderanddelivery.repository.CartItemRepository;
import com.orderanddelivery.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class TestDataSeeder implements CommandLineRunner {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedCart();
        seedOrders();
    }

    private void seedCart() {
        if (cartItemRepository.count() > 0) {
            return;
        }

        CartItem aliceParacetamol = CartItem.builder()
                .userId(2L)
                .medicineId(1L)
                .medicineName("Paracetamol 500mg")
                .unitPrice(new BigDecimal("35.00"))
                .quantity(2)
                .requiresPrescription(false)
                .substituteAllowed(true)
                .build();

        CartItem aliceVitaminC = CartItem.builder()
                .userId(2L)
                .medicineId(4L)
                .medicineName("Vitamin C Tablets")
                .unitPrice(new BigDecimal("28.00"))
                .quantity(1)
                .requiresPrescription(false)
                .substituteAllowed(true)
                .build();

        cartItemRepository.saveAll(List.of(aliceParacetamol, aliceVitaminC));
    }

    private void seedOrders() {
        if (orderRepository.count() > 0) {
            return;
        }

        Order deliveredOrder = buildOrder(
                2L,
                OrderStatus.DELIVERED,
                "221 MG Road, Bengaluru, Karnataka",
                "560001",
                "9999900001",
                "10AM - 1PM",
                null,
                List.of(
                        lineItem(1L, "Paracetamol 500mg", "35.00", 2, false),
                        lineItem(4L, "Vitamin C Tablets", "28.00", 1, false)),
                payment(PaymentStatus.SUCCESS, "UPI", "TXN-DELIVERED-1001", "98.00", null));

        Order packedOrder = buildOrder(
                2L,
                OrderStatus.PACKED,
                "14 Lake View, Bengaluru, Karnataka",
                "560034",
                "9999900001",
                "2PM - 5PM",
                null,
                List.of(
                        lineItem(3L, "Metformin 500mg", "78.00", 1, false),
                        lineItem(4L, "Vitamin C Tablets", "28.00", 2, false)),
                payment(PaymentStatus.SUCCESS, "CARD", "TXN-PACKED-1002", "134.00", null));

        Order paymentFailedOrder = buildOrder(
                3L,
                OrderStatus.PAYMENT_FAILED,
                "88 Park Street, Kolkata, West Bengal",
                "700016",
                "9999900002",
                "6PM - 9PM",
                3L,
                List.of(lineItem(2L, "Azithromycin 250mg", "110.00", 1, true)),
                payment(PaymentStatus.FAILED, "FAIL_TEST", "TXN-FAILED-1003", "110.00", "Simulated payment failure"));

        Order prescriptionPendingOrder = buildOrder(
                3L,
                OrderStatus.PRESCRIPTION_PENDING,
                "88 Park Street, Kolkata, West Bengal",
                "700016",
                "9999900002",
                "9AM - 12PM",
                2L,
                List.of(lineItem(5L, "Insulin Glargine", "760.00", 1, true)),
                null);

        orderRepository.saveAll(List.of(deliveredOrder, packedOrder, paymentFailedOrder, prescriptionPendingOrder));
    }

    private Order buildOrder(
            Long userId,
            OrderStatus status,
            String deliveryAddress,
            String deliveryPincode,
            String deliveryPhone,
            String deliverySlot,
            Long prescriptionId,
            List<OrderItemSeed> itemSeeds,
            PaymentSeed paymentSeed) {

        BigDecimal totalAmount = itemSeeds.stream()
                .map(seed -> seed.unitPrice.multiply(BigDecimal.valueOf(seed.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .userId(userId)
                .status(status)
                .deliveryAddress(deliveryAddress)
                .deliveryPincode(deliveryPincode)
                .deliveryPhone(deliveryPhone)
                .deliverySlot(deliverySlot)
                .prescriptionId(prescriptionId)
                .totalAmount(totalAmount)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(totalAmount)
                .build();

        List<OrderItem> items = itemSeeds.stream()
                .map(seed -> OrderItem.builder()
                        .order(order)
                        .medicineId(seed.medicineId)
                        .medicineName(seed.medicineName)
                        .unitPrice(seed.unitPrice)
                        .quantity(seed.quantity)
                        .subtotal(seed.unitPrice.multiply(BigDecimal.valueOf(seed.quantity)))
                        .requiresPrescription(seed.requiresPrescription)
                        .build())
                .toList();

        order.setItems(items);

        if (paymentSeed != null) {
            Payment payment = Payment.builder()
                    .order(order)
                    .status(paymentSeed.status)
                    .method(paymentSeed.method)
                    .transactionId(paymentSeed.transactionId)
                    .amount(paymentSeed.amount)
                    .failureReason(paymentSeed.failureReason)
                    .build();
            order.setPayment(payment);
        }

        return order;
    }

    private OrderItemSeed lineItem(Long medicineId, String medicineName, String unitPrice, int quantity, boolean requiresPrescription) {
        return new OrderItemSeed(medicineId, medicineName, new BigDecimal(unitPrice), quantity, requiresPrescription);
    }

    private PaymentSeed payment(PaymentStatus status, String method, String transactionId, String amount, String failureReason) {
        return new PaymentSeed(status, method, transactionId, new BigDecimal(amount), failureReason);
    }

    private record OrderItemSeed(
            Long medicineId,
            String medicineName,
            BigDecimal unitPrice,
            int quantity,
            boolean requiresPrescription) {
    }

    private record PaymentSeed(
            PaymentStatus status,
            String method,
            String transactionId,
            BigDecimal amount,
            String failureReason) {
    }
}
