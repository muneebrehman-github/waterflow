package com.spgg.studentbeans.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "student-beans")
public record StudentBeansProperties(
        String offerSlug,
        String defaultCountry,
        String connectBaseUrl,
        String mockDiscountCode,
        int mockDiscountPercent,
        int verificationTtlHours,
        List<String> corsAllowedOrigins,
        List<String> iframeAllowedOrigins) {}
