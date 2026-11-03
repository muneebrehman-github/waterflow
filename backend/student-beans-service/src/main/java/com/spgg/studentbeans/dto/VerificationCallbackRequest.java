package com.spgg.studentbeans.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record VerificationCallbackRequest(
        String studentId,
        String sbidNumber,
        String verificationToken,
        @NotBlank String offerSlug,
        @NotBlank String countryCode,
        String discountCode,
        String status,
        Map<String, Object> rawPayload) {}
