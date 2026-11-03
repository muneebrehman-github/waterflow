package com.spgg.studentbeans.dto;

import java.time.Instant;

public record VerificationResponse(
        String verificationId,
        String status,
        String offerSlug,
        String countryCode,
        String discountCode,
        Instant verifiedAt,
        Instant expiresAt) {}
