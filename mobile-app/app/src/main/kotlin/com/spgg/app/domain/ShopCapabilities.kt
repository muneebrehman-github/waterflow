package com.spgg.app.domain

/** Mirrors backend @PreAuthorize rules for shop-side operations. */
data class ShopCapabilities(
    val canManageProfile: Boolean,
    val canManageProducts: Boolean,
    val canManageTeam: Boolean,
    val canRecordPayments: Boolean,
    val canVoidPayments: Boolean,
    val canAdjustBottles: Boolean
) {
  companion object {
    fun forRole(role: UserRole): ShopCapabilities = when (role) {
      UserRole.SHOP_OWNER -> ShopCapabilities(
          canManageProfile = true,
          canManageProducts = true,
          canManageTeam = true,
          canRecordPayments = true,
          canVoidPayments = true,
          canAdjustBottles = true
      )
      UserRole.STAFF -> ShopCapabilities(
          canManageProfile = false,
          canManageProducts = false,
          canManageTeam = false,
          canRecordPayments = false,
          canVoidPayments = false,
          canAdjustBottles = false
      )
      else -> ShopCapabilities(
          canManageProfile = false,
          canManageProducts = false,
          canManageTeam = false,
          canRecordPayments = false,
          canVoidPayments = false,
          canAdjustBottles = false
      )
    }
  }
}
