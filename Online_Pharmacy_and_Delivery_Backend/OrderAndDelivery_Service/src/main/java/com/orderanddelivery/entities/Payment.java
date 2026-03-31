package com.orderanddelivery.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.orderanddelivery.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String method; // CARD, UPI, COD, NETBANKING
    private String transactionId; // payment gateway reference

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private String failureReason;

    @Column(updatable = false)
    private LocalDateTime initiatedAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        this.initiatedAt = LocalDateTime.now();
    }
}
