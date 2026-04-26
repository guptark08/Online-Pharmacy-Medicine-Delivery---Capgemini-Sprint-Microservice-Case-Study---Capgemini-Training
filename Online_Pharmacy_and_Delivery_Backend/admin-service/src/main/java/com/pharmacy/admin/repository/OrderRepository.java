package com.pharmacy.admin.repository;

import com.pharmacy.admin.entity.Order;
import com.pharmacy.admin.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ── Event-driven upsert (preserves ID from order-service) ─────
    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO orders (id, user_id, status, total_amount, discount_amount, tax_amount, delivery_charge, prescription_id, created_at, updated_at) " +
                   "VALUES (:id, :userId, :status, :totalAmount, 0, 0, 0, :prescriptionId, NOW(), NOW())", nativeQuery = true)
    int insertOrderFromEvent(@Param("id") Long id, @Param("userId") Long userId,
                             @Param("status") String status, @Param("totalAmount") double totalAmount,
                             @Param("prescriptionId") Long prescriptionId);

    // Upsert: creates the row if missing, updates status if it already exists
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO orders (id, user_id, status, total_amount, discount_amount, tax_amount, delivery_charge, created_at, updated_at) " +
                   "VALUES (:id, :userId, :status, 0, 0, 0, 0, NOW(), NOW()) " +
                   "ON DUPLICATE KEY UPDATE status = :status, updated_at = NOW()", nativeQuery = true)
    int upsertStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") String status);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO orders (id, user_id, status, total_amount, discount_amount, tax_amount, delivery_charge, payment_method, payment_id, created_at, updated_at) " +
                   "VALUES (:id, :userId, :status, 0, 0, 0, 0, :method, :paymentId, NOW(), NOW()) " +
                   "ON DUPLICATE KEY UPDATE status = :status, payment_method = :method, payment_id = :paymentId, updated_at = NOW()", nativeQuery = true)
    int upsertPayment(@Param("id") Long id, @Param("userId") Long userId, @Param("status") String status,
                      @Param("method") String method, @Param("paymentId") String paymentId);

    // ── Status filters ─────────────────────────────────────────────
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // ── User orders ────────────────────────────────────────────────
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ── Date range (for reports) ───────────────────────────────────
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end ORDER BY o.createdAt DESC")
    List<Order> findOrdersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end")
    List<Order> findByStatusAndDateRange(
            @Param("status") OrderStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ── Dashboard KPIs ─────────────────────────────────────────────
    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    double getTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED' " +
           "AND o.createdAt BETWEEN :start AND :end")
    double getRevenueForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ── Top medicines sold ─────────────────────────────────────────
    @Query("SELECT oi.medicine.name, SUM(oi.quantity) AS qty, SUM(oi.totalPrice) AS rev " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY oi.medicine.name " +
           "ORDER BY qty DESC")
    List<Object[]> findTopSellingMedicines(Pageable pageable);

    @Query("SELECT oi.medicine.name, SUM(oi.quantity) AS qty, SUM(oi.totalPrice) AS rev " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :start AND :end " +
           "GROUP BY oi.medicine.name " +
           "ORDER BY qty DESC")
    List<Object[]> findTopSellingMedicinesForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    // ── Revenue by day (for charts) ────────────────────────────────
    @Query("SELECT DATE(o.createdAt) as day, SUM(o.totalAmount) as total " +
           "FROM Order o WHERE o.status = 'DELIVERED' " +
           "AND o.createdAt BETWEEN :start AND :end " +
           "GROUP BY DATE(o.createdAt) ORDER BY day")
    List<Object[]> getDailyRevenue(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ── Orders needing attention ───────────────────────────────────
    @Query("SELECT o FROM Order o WHERE o.status IN ('PAID', 'PACKED', 'OUT_FOR_DELIVERY') " +
           "ORDER BY o.createdAt ASC")
    List<Order> findActiveOrders();
}
