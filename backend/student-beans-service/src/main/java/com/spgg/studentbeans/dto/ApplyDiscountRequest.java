package com.spgg.studentbeans.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplyDiscountRequest(
        String verificationId,
        String discountCode,
        @NotBlank String cartId) {}
