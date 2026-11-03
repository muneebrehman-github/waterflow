package com.spgg.app.ui.customer.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.CustomerRepository
import com.spgg.app.data.repository.OrderRepository
import com.spgg.app.data.repository.ProductRepository
import com.spgg.app.domain.CustomerAddress
import com.spgg.app.domain.Order
import com.spgg.app.domain.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewOrderUiState(
    val shopName: String = "",
    val products: List<Product> = emptyList(),
    val addresses: List<CustomerAddress> = emptyList(),
    val quantities: Map<Long, Int> = emptyMap(),
    val selectedAddressId: Long? = null,
    val notes: String = "",
    val isLoading: Boolean = true,
    val isPlacingOrder: Boolean = false,
    val error: String? = null,
    val placedOrder: Order? = null
) {
    val subtotal: Double
        get() = products.sumOf { (quantities[it.id] ?: 0) * it.unitPrice }

    val totalDeposit: Double
        get() = products.sumOf { (quantities[it.id] ?: 0) * it.depositAmount }

    val itemCount: Int
        get() = quantities.values.sum()
}

@HiltViewModel
class NewOrderViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewOrderUiState())
    val uiState: StateFlow<NewOrderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val profile = customerRepository.getMyProfile()
                val products = productRepository.listAvailable()
                val addresses = customerRepository.listAddresses()
                Triple(profile, products, addresses)
            }.onSuccess { (profile, products, addresses) ->
                _uiState.update {
                    it.copy(
                        shopName = profile.shopName,
                        products = products,
                        addresses = addresses,
                        selectedAddressId = addresses.find { a -> a.isDefault }?.id ?: addresses.firstOrNull()?.id,
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setQuantity(productId: Long, quantity: Int) {
        _uiState.update {
            val updated = it.quantities.toMutableMap()
            if (quantity <= 0) updated.remove(productId) else updated[productId] = quantity
            it.copy(quantities = updated)
        }
    }

    fun selectAddress(addressId: Long) {
        _uiState.update { it.copy(selectedAddressId = addressId) }
    }

    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun placeOrder() {
        val state = _uiState.value
        val addressId = state.selectedAddressId
        if (addressId == null || state.quantities.isEmpty()) {
            _uiState.update { it.copy(error = "Choose an address and at least one product") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true, error = null) }
            runCatching {
                orderRepository.placeOrder(addressId, state.quantities.toList(), state.notes.ifBlank { null })
            }.onSuccess { order ->
                _uiState.update { it.copy(isPlacingOrder = false, placedOrder = order) }
            }.onFailure { e ->
                _uiState.update { it.copy(isPlacingOrder = false, error = e.message) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
