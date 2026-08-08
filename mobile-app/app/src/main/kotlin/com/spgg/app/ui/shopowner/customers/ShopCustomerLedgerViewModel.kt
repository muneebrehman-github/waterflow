package com.spgg.app.ui.shopowner.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.BottleRepository
import com.spgg.app.data.repository.CustomerRepository
import com.spgg.app.data.repository.LedgerRepository
import com.spgg.app.data.repository.PaymentRepository
import com.spgg.app.domain.BottleBalance
import com.spgg.app.domain.LedgerEntry
import com.spgg.app.domain.Payment
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopCustomerLedgerUiState(
    val customerName: String = "",
    val balance: Double = 0.0,
    val entries: List<LedgerEntry> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val bottleBalance: BottleBalance? = null,
    val isLoading: Boolean = true,
    val isMutating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ShopCustomerLedgerViewModel @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    private val customerRepository: CustomerRepository,
    private val paymentRepository: PaymentRepository,
    private val bottleRepository: BottleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customerId: Long = checkNotNull(savedStateHandle["customerId"])

    private val _uiState = MutableStateFlow(ShopCustomerLedgerUiState())
    val uiState: StateFlow<ShopCustomerLedgerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val customer = customerRepository.getShopCustomer(customerId)
                val statement = ledgerRepository.getCustomerLedger(customerId)
                val payments = paymentRepository.listForCustomer(customerId)
                val bottleBalance = bottleRepository.getCustomerBottleBalance(customerId)
                Data(customer.fullName, statement, payments, bottleBalance)
            }
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            customerName = data.customerName,
                            balance = data.statement.balance,
                            entries = data.statement.entries,
                            payments = data.payments,
                            bottleBalance = data.bottleBalance,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun recordPayment(amount: Double, method: String, reference: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true, error = null) }
            runCatching { paymentRepository.record(customerId, amount, method, reference) }
                .onSuccess { _uiState.update { it.copy(isMutating = false) }; load() }
                .onFailure { e -> _uiState.update { it.copy(isMutating = false, error = e.message) } }
        }
    }

    fun voidPayment(paymentId: Long, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true, error = null) }
            runCatching { paymentRepository.voidPayment(paymentId, reason) }
                .onSuccess { _uiState.update { it.copy(isMutating = false) }; load() }
                .onFailure { e -> _uiState.update { it.copy(isMutating = false, error = e.message) } }
        }
    }

    fun markBottleAdjustment(quantity: Int, type: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true, error = null) }
            runCatching { bottleRepository.markAdjustment(customerId, quantity, type, reason) }
                .onSuccess { balance -> _uiState.update { it.copy(isMutating = false, bottleBalance = balance) } }
                .onFailure { e -> _uiState.update { it.copy(isMutating = false, error = e.message) } }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private data class Data(
        val customerName: String,
        val statement: com.spgg.app.domain.LedgerStatement,
        val payments: List<Payment>,
        val bottleBalance: BottleBalance
    )
}
