package com.spgg.studentbeans.service;

import com.spgg.studentbeans.common.ResourceNotFoundException;
import com.spgg.studentbeans.config.StudentBeansProperties;
import com.spgg.studentbeans.dto.ApplyDiscountRequest;
import com.spgg.studentbeans.dto.ApplyDiscountResponse;
import com.spgg.studentbeans.dto.ConnectUrlResponse;
import com.spgg.studentbeans.dto.VerificationCallbackRequest;
import com.spgg.studentbeans.dto.VerificationResponse;
import com.spgg.studentbeans.model.StudentVerification;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StudentDiscountService {

    private static final Logger log = LoggerFactory.getLogger(StudentDiscountService.class);

    private final StudentBeansProperties properties;
    private final Map<String, StudentVerification> verifications = new ConcurrentHashMap<>();
    private final Map<String, String> cartDiscounts = new ConcurrentHashMap<>();

    public StudentDiscountService(StudentBeansProperties properties) {
        this.properties = properties;
    }

    public ConnectUrlResponse getConnectUrl(String countryCode, String offerSlug) {
        var country = normalizeCountry(countryCode);
        var slug = StringUtils.hasText(offerSlug) ? offerSlug.trim() : properties.offerSlug();
        var connectUrl = "%s/%s/%s/".formatted(properties.connectBaseUrl(), slug, country);

        return new ConnectUrlResponse(
                connectUrl,
                slug,
                country,
                properties.iframeAllowedOrigins());
    }

    public VerificationResponse handleVerificationCallback(VerificationCallbackRequest request) {
        validateOffer(request.offerSlug(), request.countryCode());

        var verificationId = UUID.randomUUID().toString();
        var now = Instant.now();
        var expiresAt = now.plus(properties.verificationTtlHours(), ChronoUnit.HOURS);
        var discountCode = StringUtils.hasText(request.discountCode())
                ? request.discountCode().trim()
                : properties.mockDiscountCode();
        var status = StringUtils.hasText(request.status()) ? request.status().trim() : "verified";
        var studentId = firstNonBlank(request.studentId(), request.sbidNumber(), "anonymous-" + verificationId.substring(0, 8));

        var verification = new StudentVerification(
                verificationId,
                studentId,
                request.sbidNumber(),
                request.verificationToken(),
                request.offerSlug(),
                normalizeCountry(request.countryCode()),
                discountCode,
                status,
                request.rawPayload(),
                now,
                expiresAt,
                false);

        verifications.put(verificationId, verification);
        log.info(
                "Stored verification {} for student {} (offer={}, country={})",
                verificationId,
                studentId,
                request.offerSlug(),
                request.countryCode());

        return toResponse(verification);
    }

    public ApplyDiscountResponse applyDiscount(ApplyDiscountRequest request) {
        var verification = resolveVerification(request.verificationId(), request.discountCode());

        if (!verification.isActive()) {
            if (verification.isExpired()) {
                throw new IllegalStateException("Verification has expired");
            }
            if (verification.applied()) {
                throw new IllegalStateException("Discount already applied for this verification");
            }
            throw new IllegalStateException("Verification is not in a valid state: " + verification.status());
        }

        var updated = new StudentVerification(
                verification.verificationId(),
                verification.studentId(),
                verification.sbidNumber(),
                verification.verificationToken(),
                verification.offerSlug(),
                verification.countryCode(),
                verification.discountCode(),
                verification.status(),
                verification.rawPayload(),
                verification.verifiedAt(),
                verification.expiresAt(),
                true);

        verifications.put(updated.verificationId(), updated);
        cartDiscounts.put(request.cartId(), updated.discountCode());

        log.info(
                "Applied discount {} to cart {} (verification={})",
                updated.discountCode(),
                request.cartId(),
                updated.verificationId());

        return new ApplyDiscountResponse(
                true,
                request.cartId(),
                updated.discountCode(),
                properties.mockDiscountPercent(),
                "Student discount applied successfully");
    }

    public VerificationResponse getVerification(String verificationId) {
        return toResponse(findVerification(verificationId));
    }

    public List<VerificationResponse> listVerifications() {
        return verifications.values().stream().map(this::toResponse).toList();
    }

    public String getCartDiscountCode(String cartId) {
        return cartDiscounts.get(cartId);
    }

    private StudentVerification resolveVerification(String verificationId, String discountCode) {
        if (StringUtils.hasText(verificationId)) {
            return findVerification(verificationId);
        }

        if (StringUtils.hasText(discountCode)) {
            return verifications.values().stream()
                    .filter(item -> discountCode.equalsIgnoreCase(item.discountCode()))
                    .filter(StudentVerification::isActive)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No active verification for discount code"));
        }

        throw new IllegalArgumentException("Either verificationId or discountCode is required");
    }

    private StudentVerification findVerification(String verificationId) {
        var verification = verifications.get(verificationId);
        if (verification == null) {
            throw new ResourceNotFoundException("Verification not found: " + verificationId);
        }
        return verification;
    }

    private void validateOffer(String offerSlug, String countryCode) {
        if (!properties.offerSlug().equalsIgnoreCase(offerSlug.trim())) {
            log.warn("Unexpected offer slug {} (configured={})", offerSlug, properties.offerSlug());
        }
        if (!properties.defaultCountry().equalsIgnoreCase(normalizeCountry(countryCode))) {
            log.warn("Unexpected country {} (default={})", countryCode, properties.defaultCountry());
        }
    }

    private VerificationResponse toResponse(StudentVerification verification) {
        return new VerificationResponse(
                verification.verificationId(),
                verification.status(),
                verification.offerSlug(),
                verification.countryCode(),
                verification.discountCode(),
                verification.verifiedAt(),
                verification.expiresAt());
    }

    private String normalizeCountry(String countryCode) {
        if (!StringUtils.hasText(countryCode)) {
            return properties.defaultCountry().toLowerCase(Locale.ROOT);
        }
        return countryCode.trim().toLowerCase(Locale.ROOT);
    }

    private String firstNonBlank(String... values) {
        for (var value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
