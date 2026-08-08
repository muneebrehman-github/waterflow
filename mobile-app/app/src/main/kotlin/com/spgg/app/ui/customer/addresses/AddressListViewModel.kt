package com.spgg.app.ui.customer.addresses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.CustomerRepository
import com.spgg.app.domain.CustomerAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddressListUiState(
    val addresses: List<CustomerAddress> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddressListViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressListUiState())
    val uiState: StateFlow<AddressListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.listAddresses() }
                .onSuccess { addresses -> _uiState.update { it.copy(addresses = addresses, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun createAddress(label: String, addressText: String, area: String?, isDefault: Boolean) = mutate {
        repository.createAddress(label, addressText, area, isDefault)
    }

    fun updateAddress(id: Long, label: String, addressText: String, area: String?, isDefault: Boolean) = mutate {
        repository.updateAddress(id, label, addressText, area, isDefault)
    }

    fun setDefault(id: Long) = mutate { repository.setDefaultAddress(id) }

    fun delete(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching { repository.deleteAddress(id) }
                .onSuccess { load() }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun mutate(block: suspend () -> CustomerAddress) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching { block() }
                .onSuccess { load() }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
        }
    }
}
