package com.orderanddelivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.orderanddelivery.entities.Order;
import com.orderanddelivery.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<Order> findByPrescriptionId(Long prescriptionId);
}
