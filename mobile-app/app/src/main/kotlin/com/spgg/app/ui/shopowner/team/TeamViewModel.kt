package com.spgg.app.ui.shopowner.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.StaffRepository
import com.spgg.app.domain.StaffSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamUiState(
    val members: List<StaffSummary> = emptyList(),
    val isLoading: Boolean = true,
    val isMutating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val repository: StaffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.list() }
                .onSuccess { members -> _uiState.update { it.copy(members = members, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun createMember(fullName: String, phone: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true, error = null) }
            runCatching { repository.create(fullName, phone, email, password, role) }
                .onSuccess { _uiState.update { it.copy(isMutating = false) }; load() }
                .onFailure { e -> _uiState.update { it.copy(isMutating = false, error = e.message) } }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
