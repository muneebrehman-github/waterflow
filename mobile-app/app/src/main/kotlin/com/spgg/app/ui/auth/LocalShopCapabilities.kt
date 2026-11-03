package com.spgg.app.ui.auth

import androidx.compose.runtime.compositionLocalOf
import com.spgg.app.domain.ShopCapabilities

val LocalShopCapabilities = compositionLocalOf { ShopCapabilities.forRole(com.spgg.app.domain.UserRole.CUSTOMER) }
