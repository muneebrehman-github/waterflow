package com.spgg.app.ui.shopowner.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.CustomerRepository
import com.spgg.app.domain.CustomerSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopCustomersUiState(
    val customers: List<CustomerSummary> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ShopCustomersViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopCustomersUiState())
    val uiState: StateFlow<ShopCustomersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.listShopCustomers() }
                .onSuccess { customers -> _uiState.update { it.copy(customers = customers, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
