package com.spgg.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgg.app.data.repository.AuthRepository
import com.spgg.app.domain.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Unknown)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    init {
        val existing = authRepository.restoreSession()
        _uiState.value = if (existing != null) {
            AuthUiState.Authenticated(existing)
        } else {
            AuthUiState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            _loginError.value = null

            authRepository.login(email, password)
                .onSuccess { session ->
                    if (session.role == UserRole.SUPER_ADMIN) {
                        authRepository.logout()
                        _loginError.value = "Super Admin accounts use the web admin portal, not this app."
                        _uiState.value = AuthUiState.Unauthenticated
                    } else {
                        _uiState.value = AuthUiState.Authenticated(session)
                    }
                }
                .onFailure { error ->
                    _loginError.value = error.message ?: "Something went wrong. Please try again."
                }

            _isLoggingIn.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Unauthenticated
        }
    }
}
