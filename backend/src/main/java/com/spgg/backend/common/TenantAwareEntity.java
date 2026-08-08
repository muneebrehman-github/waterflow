package com.spgg.backend.common;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Base for tenant-owned business entities (Product, Customer, Order, Ledger, ...).
 * Subclasses are automatically scoped to the current tenant via the
 * "tenantFilter" Hibernate filter, enabled per-request by {@link TenantFilter}
 * for authenticated shop-scoped requests (Super Admin requests have no tenant
 * in {@link TenantContext} and see the filter left disabled for that request —
 * intentional, since Super Admin is platform-scoped, not tenant-scoped).
 *
 * <p>Nothing in this slice extends this class yet (Shop is the tenant record
 * itself, not tenant-owned data). It's introduced now so the next feature
 * slices (Product, Customer, Order, ...) can extend it directly.
 */
@Getter
@Setter
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
