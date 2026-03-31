package com.pharmacy.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String genericName;

    @Column(length = 200)
    private String manufacturer;

    // tablet, syrup, injection, capsule, cream, drops
    @Column(length = 50)
    private String dosageForm;

    // e.g. 500mg, 10ml
    @Column(length = 50)
    private String strength;

    @Column(nullable = false)
    private double price;

    private double mrp;

    @Column(nullable = false)
    @Builder.Default
    private int stock = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean requiresPrescription = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    // HSN code / SKU for inventory tracking
    @Column(unique = true, length = 50)
    private String sku;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
