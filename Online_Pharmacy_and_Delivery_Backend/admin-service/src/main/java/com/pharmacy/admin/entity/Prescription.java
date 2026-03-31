package com.pharmacy.admin.entity;

import com.pharmacy.admin.enums.PrescriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 200)
    private String userEmail;

    // File path / URL stored after upload
    @Column(nullable = false, length = 500)
    private String fileUrl;

    @Column(length = 10)
    private String fileType;  // PDF, JPG, PNG

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PrescriptionStatus status = PrescriptionStatus.PENDING;

    // Doctor details (optional — user provides)
    @Column(length = 200)
    private String doctorName;

    @Column(length = 100)
    private String doctorRegNumber;

    // Admin who reviewed it
    private Long reviewedByAdminId;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // Linked order (set when prescription is approved for an order)
    private Long orderId;

    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
