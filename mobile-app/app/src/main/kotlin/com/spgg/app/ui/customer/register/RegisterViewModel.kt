package com.spgg.app.ui.customer.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { repository.shopDirectory() }
                .onSuccess { shops -> _uiState.update { it.copy(shops = shops, isLoadingShops = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoadingShops = false, error = e.message) } }
        }
    }

    /** Returns true on success — caller (RegisterScreen) then logs the new account in. */
    suspend fun register(shopId: Long, fullName: String, phone: String, email: String, password: String): Boolean {
        _uiState.update { it.copy(isRegistering = true, error = null) }
        val result = runCatching { repository.register(shopId, fullName, phone, email, password) }
        _uiState.update { it.copy(isRegistering = false) }
        result.onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        return result.isSuccess
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
