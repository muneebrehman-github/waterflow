package com.spgg.app.domain

/** Mirrors the backend's Role enum (com.spgg.backend.user.Role) — names match the wire format exactly. */
enum class UserRole {
    SUPER_ADMIN,
    SHOP_OWNER,
    STAFF,
    DELIVERY_BOY,
    CUSTOMER
}
