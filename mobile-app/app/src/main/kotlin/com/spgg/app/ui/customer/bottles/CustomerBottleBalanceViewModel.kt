package com.spgg.app.ui.customer.bottles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.BottleRepository
import com.spgg.app.domain.BottleBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerBottleBalanceUiState(
    val balance: BottleBalance? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CustomerBottleBalanceViewModel @Inject constructor(
    private val repository: BottleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerBottleBalanceUiState())
    val uiState: StateFlow<CustomerBottleBalanceUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getMyBottleBalance() }
                .onSuccess { balance -> _uiState.update { it.copy(balance = balance, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
