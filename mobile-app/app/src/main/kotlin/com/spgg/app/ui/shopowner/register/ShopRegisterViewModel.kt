package com.spgg.app.ui.shopowner.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ShopRegisterViewModel @Inject constructor(
    private val repository: ShopRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopRegisterUiState())
    val uiState: StateFlow<ShopRegisterUiState> = _uiState.asStateFlow()

    fun register(
        shopName: String,
        ownerName: String,
        email: String,
        phone: String,
        password: String,
        description: String,
        address: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRegistering = true, error = null) }
            runCatching {
                repository.register(shopName, ownerName, email, phone, password, description, address)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isRegistering = false,
                        registrationComplete = true,
                        registeredShopName = shopName
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isRegistering = false, error = e.message) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
