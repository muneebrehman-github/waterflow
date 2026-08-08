package com.spgg.app.ui.shopowner.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.ShopProfileRepository
import com.spgg.app.domain.DayHours
import com.spgg.app.domain.DeliveryChargeTier
import com.spgg.app.domain.ShopProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ShopProfileViewModel @Inject constructor(
    private val repository: ShopProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopProfileUiState())
    val uiState: StateFlow<ShopProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getProfile() }
                .onSuccess { profile -> _uiState.update { it.copy(profile = profile, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun updateBasicInfo(name: String, description: String?, address: String?) =
        mutate { repository.updateProfile(name, description, address) }

    fun updateBusinessHours(hours: List<DayHours>) =
        mutate { repository.updateBusinessHours(hours) }

    fun updateDeliveryHours(hours: List<DayHours>) =
        mutate { repository.updateDeliveryHours(hours) }

    fun updateDeliverySettings(radiusKm: Double, minOrderValue: Double, tiers: List<DeliveryChargeTier>) =
        mutate { repository.updateDeliverySettings(radiusKm, minOrderValue, tiers) }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun mutate(block: suspend () -> ShopProfile) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching { block() }
                .onSuccess { profile -> _uiState.update { it.copy(profile = profile, isSaving = false) } }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
        }
    }
}
