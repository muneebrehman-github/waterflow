package com.spgg.app.ui.shopowner.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.ProductRepository
import com.spgg.app.domain.Product
import com.spgg.app.domain.ProductPriceHistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.listAll() }
                .onSuccess { products -> _uiState.update { it.copy(products = products, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun createProduct(name: String, unitPrice: Double, depositAmount: Double, displayOrder: Int?) = mutate {
        repository.create(name, unitPrice, depositAmount, displayOrder)
    }

    fun updateProduct(id: Long, name: String, depositAmount: Double, displayOrder: Int?) = mutate {
        repository.update(id, name, depositAmount, displayOrder)
    }

    fun updatePrice(id: Long, unitPrice: Double) = mutate {
        repository.updatePrice(id, unitPrice)
    }

    fun toggleAvailability(id: Long) = mutate {
        repository.toggleAvailability(id)
    }

    fun archive(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching { repository.archive(id) }
                .onSuccess { load() }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    suspend fun loadPriceHistory(id: Long): Result<List<ProductPriceHistoryEntry>> =
        runCatching { repository.priceHistory(id) }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun mutate(block: suspend () -> Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching { block() }
                .onSuccess { load() }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
        }
    }
}
