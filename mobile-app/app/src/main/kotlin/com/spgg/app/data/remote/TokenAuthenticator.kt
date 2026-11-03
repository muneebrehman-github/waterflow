package com.spgg.app.data.remote

import com.spgg.app.data.local.SessionStore
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Invoked by OkHttp automatically on a 401 (or 403 when the caller sent a
 * Bearer token, covering legacy backend responses). Uses a raw AuthApi (built
 * from a client with no interceptor/authenticator of its own — see NetworkModule)
 * to avoid recursing back into itself while refreshing.
 */
class TokenAuthenticator @Inject constructor(
    private val sessionStore: SessionStore,
    @RawAuthApi private val rawAuthApi: AuthApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (!shouldAttemptRefresh(response)) return null
        if (priorResponseCount(response) >= 2) return null // already retried once — give up

        val session = sessionStore.getSession() ?: return null

        val newAccessToken = runBlocking {
            runCatching { rawAuthApi.refresh(RefreshRequestDto(session.refreshToken)).data?.accessToken }
                .getOrNull()
        }

        if (newAccessToken == null) {
            sessionStore.clearSession()
            return null
        }

        sessionStore.updateAccessToken(newAccessToken)
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun priorResponseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private fun shouldAttemptRefresh(response: Response): Boolean {
        if (response.code == 401) return true
        if (response.code != 403) return false
        // Only treat 403 as "token stale" when we actually sent credentials.
        return response.request.header("Authorization")?.startsWith("Bearer ") == true
    }
}
