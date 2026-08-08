package com.spgg.app.ui.deliveryboy

import androidx.lifecycle.SavedStateHandle
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

data class DeliveryDetailUiState(
    val order: Order? = null,
    val isLoading: Boolean = true,
    val isMutating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DeliveryDetailViewModel @Inject constructor(
    private val repository: OrderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(DeliveryDetailUiState())
    val uiState: StateFlow<DeliveryDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getAssignedDelivery(orderId) }
                .onSuccess { order -> _uiState.update { it.copy(order = order, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun completeDelivery(emptyBottlesCollected: Int?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true, error = null) }
            runCatching { repository.completeDelivery(orderId, emptyBottlesCollected) }
                .onSuccess { order -> _uiState.update { it.copy(order = order, isMutating = false) } }
                .onFailure { e -> _uiState.update { it.copy(isMutating = false, error = e.message) } }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
