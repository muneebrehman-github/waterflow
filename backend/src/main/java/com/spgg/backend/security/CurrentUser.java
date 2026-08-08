package com.spgg.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;

/** JwtAuthenticationFilter sets the authenticated userId as the Authentication principal. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
