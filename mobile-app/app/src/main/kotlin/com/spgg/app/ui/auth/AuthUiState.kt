package com.spgg.app.ui.auth

import com.spgg.app.domain.AuthSession

sealed interface AuthUiState {
    data object Unknown : AuthUiState
    data object Unauthenticated : AuthUiState
    data class Authenticated(val session: AuthSession) : AuthUiState
}
