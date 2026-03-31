package com.orderanddelivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orderanddelivery.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByOrderIdAndOrderUserId(Long orderId, Long userId);
}
