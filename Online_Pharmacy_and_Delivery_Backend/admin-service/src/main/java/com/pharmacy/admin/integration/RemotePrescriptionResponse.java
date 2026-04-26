package com.pharmacy.admin.integration;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RemotePrescriptionResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String fileUrl;
    private String fileType;
    private String status;
    private String doctorName;
    private String doctorRegNumber;
    private Long reviewedByAdminId;
    private String rejectionReason;
    private Long orderId;
    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;
}