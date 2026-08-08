package com.spgg.app.ui.shopowner.products

import com.spgg.app.domain.Product

data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)
