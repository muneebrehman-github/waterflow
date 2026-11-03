package com.spgg.app.ui.shopowner.register

data class ShopRegisterUiState(
    val isRegistering: Boolean = false,
    val registrationComplete: Boolean = false,
    val registeredShopName: String = "",
    val error: String? = null
)
