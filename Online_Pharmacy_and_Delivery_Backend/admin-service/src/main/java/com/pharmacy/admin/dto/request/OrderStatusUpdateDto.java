package com.pharmacy.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusUpdateDto {

    @NotBlank(message = "Status is required")
    private String status;      // e.g. "PACKED", "OUT_FOR_DELIVERY"

    private String adminNote;   // optional reason or message from admin
}
