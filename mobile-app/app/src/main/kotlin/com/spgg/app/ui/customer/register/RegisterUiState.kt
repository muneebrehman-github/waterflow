package com.spgg.app.ui.customer.register

import com.spgg.app.domain.ShopDirectoryEntry

data class RegisterUiState(
    val shops: List<ShopDirectoryEntry> = emptyList(),
    val isLoadingShops: Boolean = true,
    val isRegistering: Boolean = false,
    val error: String? = null
)
