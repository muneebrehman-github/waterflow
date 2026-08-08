package com.spgg.backend.shop;

import com.spgg.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Shop IS the tenant (see SRS glossary) — every other tenant-owned entity
 * carries this id as its {@code tenant_id}. Deliberately extends
 * {@link BaseEntity}, not {@code TenantAwareEntity}: this table is never
 * itself filtered by tenant, since Super Admin must see across all shops
 * and a Shop Owner's own shop is looked up by primary key, not by a
 * tenant-scoped query.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shops")
public class Shop extends BaseEntity {

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShopStatus status = ShopStatus.PENDING_APPROVAL;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    @Column(name = "business_hours_json")
    private String businessHoursJson;

    @Column(name = "delivery_hours_json")
    private String deliveryHoursJson;

    @Column(name = "delivery_radius_km")
    private BigDecimal deliveryRadiusKm;

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    @Column(name = "delivery_charge_tiers_json")
    private String deliveryChargeTiersJson;
}
