package com.spgg.app.data.repository

import com.spgg.app.data.local.SessionStore
import com.spgg.app.data.remote.AuthApi
import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.LoginRequestDto
import com.spgg.app.data.remote.RefreshRequestDto
import com.spgg.app.domain.ApiException
import com.spgg.app.domain.AuthSession
import com.spgg.app.domain.UserRole
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
    private val json: Json
) {

    suspend fun login(email: String, password: String): Result<AuthSession> = runCatching {
        val response = authApi.login(LoginRequestDto(email, password))
        val data = response.data
        if (!response.success || data == null) {
            throw ApiException(response.message ?: "Login failed")
        }
        val session = AuthSession(
            accessToken = data.accessToken,
            refreshToken = data.refreshToken,
            userId = data.userId,
            fullName = data.fullName,
            role = UserRole.valueOf(data.role),
            tenantId = data.tenantId
        )
        sessionStore.saveSession(session)
        session
    }.recoverCatching { throw mapError(it) }

    fun restoreSession(): AuthSession? = sessionStore.getSession()

    suspend fun logout() {
        val session = sessionStore.getSession()
        if (session != null) {
            runCatching { authApi.logout(RefreshRequestDto(session.refreshToken)) }
        }
        sessionStore.clearSession()
    }

    private fun mapError(t: Throwable): Throwable = when (t) {
        is HttpException -> {
            val message = runCatching {
                t.response()?.errorBody()?.string()?.let { body ->
                    json.decodeFromString(ErrorEnvelope.serializer(), body).message
                }
            }.getOrNull()
            ApiException(message ?: "Something went wrong. Please try again.")
        }
        is IOException -> ApiException("Unable to reach the server. Check your connection.")
        is ApiException -> t
        else -> ApiException(t.message ?: "Something went wrong. Please try again.")
    }
}
