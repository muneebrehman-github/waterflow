package com.spgg.app.ui.shopowner.profile

import com.spgg.app.domain.ShopProfile

data class ShopProfileUiState(
    val profile: ShopProfile? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)
