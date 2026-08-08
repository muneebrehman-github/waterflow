package com.spgg.app.ui.shopowner.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.OrderRepository
import com.spgg.app.data.repository.StaffRepository
import com.spgg.app.domain.Order
import com.spgg.app.domain.StaffSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopOrderDetailUiState(
    val order: Order? = null,
    val deliveryBoys: List<StaffSummary> = emptyList(),
    val isLoading: Boolean = true,
    val isMutating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ShopOrderDetailViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val staffRepository: StaffRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(ShopOrderDetailUiState())
    val uiState: StateFlow<ShopOrderDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val order = repository.getShopOrder(orderId)
                val deliveryBoys = staffRepository.list("DELIVERY_BOY")
                order to deliveryBoys
            }
                .onSuccess { (order, deliveryBoys) -> _uiState.update { it.copy(order = order, deliveryBoys = deliveryBoys, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun accept() = mutate { repository.acceptOrder(orderId) }

    fun reject(reason: String) = mutate { repository.rejectOrder(orderId, reason) }

    fun prepare() = mutate { repository.prepareOrder(orderId) }

    fun cancel(reason: String) = mutate { repository.cancelShopOrder(orderId, reason) }

    fun assignDelivery(deliveryBoyId: Long) = mutate { repository.assignDelivery(orderId, deliveryBoyId) }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun mutate(block: suspend () -> Order) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true, error = null) }
            runCatching { block() }
                .onSuccess { order -> _uiState.update { it.copy(order = order, isMutating = false) } }
                .onFailure { e -> _uiState.update { it.copy(isMutating = false, error = e.message) } }
        }
    }
}
