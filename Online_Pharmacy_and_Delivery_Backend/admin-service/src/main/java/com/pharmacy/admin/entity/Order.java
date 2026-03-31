package com.pharmacy.admin.entity;

import com.pharmacy.admin.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to user from Identity Service (no FK across microservices)
    @Column(nullable = false)
    private Long userId;

    @Column(length = 200)
    private String userEmail;

    @Column(length = 200)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @Column(nullable = false)
    private double totalAmount;

    private double discountAmount;
    private double taxAmount;
    private double deliveryCharge;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // Delivery details
    @Column(length = 500)
    private String deliveryAddress;

    @Column(length = 10)
    private String pincode;

    @Column(length = 20)
    private String deliverySlot;

    // Prescription reference (if order has Rx medicines)
    private Long prescriptionId;

    // Admin notes (used when admin cancels, rejects prescription, etc.)
    @Column(columnDefinition = "TEXT")
    private String adminNote;

    // Payment details
    @Column(length = 100)
    private String paymentId;

    @Column(length = 50)
    private String paymentMethod;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
