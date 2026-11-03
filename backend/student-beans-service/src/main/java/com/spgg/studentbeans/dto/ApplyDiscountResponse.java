package com.spgg.studentbeans.dto;

public record ApplyDiscountResponse(
        boolean applied,
        String cartId,
        String discountCode,
        int discountPercent,
        String message) {}
