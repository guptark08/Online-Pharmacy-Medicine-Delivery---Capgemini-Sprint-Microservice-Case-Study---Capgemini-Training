package com.pharmacy.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponseDto {

    private Long id;
    private Long userId;
    private String userEmail;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private String status;
    private String doctorName;
    private String doctorRegNumber;
    private Long reviewedByAdminId;
    private String rejectionReason;
    private Long orderId;
    private String uploadedAt;
    private String reviewedAt;
}
