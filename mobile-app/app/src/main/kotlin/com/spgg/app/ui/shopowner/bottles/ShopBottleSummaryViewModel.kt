package com.spgg.app.ui.shopowner.bottles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.BottleRepository
import com.spgg.app.domain.ShopBottleSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopBottleSummaryUiState(
    val summary: ShopBottleSummary? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ShopBottleSummaryViewModel @Inject constructor(
    private val repository: BottleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopBottleSummaryUiState())
    val uiState: StateFlow<ShopBottleSummaryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getShopSummary() }
                .onSuccess { summary -> _uiState.update { it.copy(summary = summary, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
