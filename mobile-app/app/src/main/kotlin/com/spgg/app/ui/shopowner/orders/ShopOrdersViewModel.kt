package com.spgg.app.ui.shopowner.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.OrderRepository
import com.spgg.app.domain.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopOrdersUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ShopOrdersViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopOrdersUiState())
    val uiState: StateFlow<ShopOrdersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.listShopOrders() }
                .onSuccess { orders -> _uiState.update { it.copy(orders = orders, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
