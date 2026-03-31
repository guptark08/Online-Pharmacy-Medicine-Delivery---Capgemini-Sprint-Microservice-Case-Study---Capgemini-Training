package com.orderanddelivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orderanddelivery.entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	List<OrderItem> findByOrderId(Long orderId);
}