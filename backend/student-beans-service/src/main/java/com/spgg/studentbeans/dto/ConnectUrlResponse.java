package com.spgg.studentbeans.dto;

import java.util.List;

public record ConnectUrlResponse(
        String connectUrl,
        String offerSlug,
        String countryCode,
        List<String> iframeAllowedOrigins) {}
