package com.spgg.backend.user;

import com.spgg.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Identity/credential record for every human actor on the platform (Super
 * Admin, Shop Owner, Staff, Delivery Boy, Customer). Role-specific profile
 * data (e.g. Customer addresses) lives in later slices' own tables, linked
 * back to this row.
 *
 * <p>{@code tenantId} is null for SUPER_ADMIN (platform-scoped, FR-AUTH-011)
 * and set to the owning shop's id for every other role. This entity does
 * NOT extend {@link com.spgg.backend.common.TenantAwareEntity} — login needs
 * an unfiltered cross-tenant lookup by email, so tenant scoping for user
 * management endpoints (e.g. "list my shop's staff") should be applied
 * explicitly via query parameters when that feature is built, rather than
 * relying on the global Hibernate filter for identity records.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
}
