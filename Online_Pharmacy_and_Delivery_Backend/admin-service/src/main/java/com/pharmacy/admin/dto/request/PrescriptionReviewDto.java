package com.pharmacy.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrescriptionReviewDto {

    @NotBlank(message = "Decision is required: APPROVED or REJECTED")
    private String decision;        // "APPROVED" or "REJECTED"

    private String rejectionReason; // required when decision = REJECTED
}
