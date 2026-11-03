package com.spgg.studentbeans.model;

import java.time.Instant;
import java.util.Map;

public record StudentVerification(
        String verificationId,
        String studentId,
        String sbidNumber,
        String verificationToken,
        String offerSlug,
        String countryCode,
        String discountCode,
        String status,
        Map<String, Object> rawPayload,
        Instant verifiedAt,
        Instant expiresAt,
        boolean applied) {

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return "verified".equalsIgnoreCase(status) && !isExpired() && !applied;
    }
}
