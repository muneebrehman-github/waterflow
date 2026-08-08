package com.spgg.backend.common;

/**
 * Per-request holder for the authenticated caller's tenant (shop) id.
 * Populated by the JWT auth filter, cleared at the end of every request.
 * Null means "no tenant" — either Super Admin (platform-scoped) or an
 * unauthenticated request.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
