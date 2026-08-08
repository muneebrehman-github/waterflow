package com.spgg.app.ui.customer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.CustomerRepository
import com.spgg.app.domain.CustomerProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerProfileUiState(
    val profile: CustomerProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CustomerProfileViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerProfileUiState())
    val uiState: StateFlow<CustomerProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { repository.getMyProfile() }
                .onSuccess { profile -> _uiState.update { it.copy(profile = profile, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
