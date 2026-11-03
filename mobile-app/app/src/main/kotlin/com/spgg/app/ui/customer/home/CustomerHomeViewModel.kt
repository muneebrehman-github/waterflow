package com.spgg.app.ui.customer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.BottleRepository
import com.spgg.app.data.repository.CustomerRepository
import com.spgg.app.data.repository.LedgerRepository
import com.spgg.app.data.repository.OrderRepository
import com.spgg.app.domain.BottleBalance
import com.spgg.app.domain.CustomerAddress
import com.spgg.app.domain.CustomerProfile
import com.spgg.app.domain.LedgerEntry
import com.spgg.app.domain.Order
import com.spgg.app.ui.components.ActiveOrderStatuses
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerHomeUiState(
    val profile: CustomerProfile? = null,
    val orders: List<Order> = emptyList(),
    val addresses: List<CustomerAddress> = emptyList(),
    val ledgerBalance: Double = 0.0,
    val ledgerEntries: List<LedgerEntry> = emptyList(),
    val bottles: BottleBalance? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    /** Most recent order that hasn't reached a terminal state. */
    val activeOrder: Order?
        get() = orders.filter { it.status in ActiveOrderStatuses }.maxByOrNull { it.id }

    val defaultAddress: CustomerAddress?
        get() = addresses.find { it.isDefault } ?: addresses.firstOrNull()
}

/** Backs every tab of the customer home shell (home, orders, wallet, account)
 *  so a single pull-to-refresh keeps them all consistent. */
@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val orderRepository: OrderRepository,
    private val ledgerRepository: LedgerRepository,
    private val bottleRepository: BottleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerHomeUiState())
    val uiState: StateFlow<CustomerHomeUiState> = _uiState.asStateFlow()

    fun refresh(userInitiated: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = userInitiated, error = null) }
            // Each section loads independently so one failing endpoint doesn't blank the whole home.
            coroutineScope {
                val profile = async { runCatching { customerRepository.getMyProfile() } }
                val orders = async { runCatching { orderRepository.listMyOrders() } }
                val addresses = async { runCatching { customerRepository.listAddresses() } }
                val ledger = async { runCatching { ledgerRepository.getMyLedger() } }
                val bottles = async { runCatching { bottleRepository.getMyBottleBalance() } }

                val results = listOf(profile.await(), orders.await(), addresses.await(), ledger.await(), bottles.await())
                _uiState.update { state ->
                    state.copy(
                        profile = profile.await().getOrNull() ?: state.profile,
                        orders = orders.await().getOrNull()?.sortedByDescending { it.id } ?: state.orders,
                        addresses = addresses.await().getOrNull() ?: state.addresses,
                        ledgerBalance = ledger.await().getOrNull()?.balance ?: state.ledgerBalance,
                        ledgerEntries = ledger.await().getOrNull()?.entries ?: state.ledgerEntries,
                        bottles = bottles.await().getOrNull() ?: state.bottles,
                        isLoading = false,
                        isRefreshing = false,
                        error = results.firstNotNullOfOrNull { it.exceptionOrNull()?.message }
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
