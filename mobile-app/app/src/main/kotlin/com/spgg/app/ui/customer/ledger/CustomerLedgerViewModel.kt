package com.spgg.app.ui.customer.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.LedgerRepository
import com.spgg.app.domain.LedgerEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerLedgerUiState(
    val balance: Double = 0.0,
    val entries: List<LedgerEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CustomerLedgerViewModel @Inject constructor(
    private val repository: LedgerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerLedgerUiState())
    val uiState: StateFlow<CustomerLedgerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getMyLedger() }
                .onSuccess { statement -> _uiState.update { it.copy(balance = statement.balance, entries = statement.entries, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
