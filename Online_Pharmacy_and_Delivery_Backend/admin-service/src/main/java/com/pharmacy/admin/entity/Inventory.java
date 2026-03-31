package com.pharmacy.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false, length = 100)
    private String batchNumber;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double costPrice;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private LocalDate manufactureDate;

    @Column(length = 200)
    private String supplier;

    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (receivedAt == null) receivedAt = LocalDateTime.now();
    }
}
