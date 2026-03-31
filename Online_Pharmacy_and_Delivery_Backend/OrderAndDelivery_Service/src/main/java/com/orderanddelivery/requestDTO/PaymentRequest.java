package com.orderanddelivery.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be positive")
    private Long orderId;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "CARD|UPI|COD|NETBANKING|FAIL_TEST", message = "Invalid payment method")
    private String method;

    private String transactionRef;
}