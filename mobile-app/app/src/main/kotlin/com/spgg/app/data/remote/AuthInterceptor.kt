package com.spgg.app.data.remote

import com.spgg.app.data.local.SessionStore
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

/** Attaches the cached access token to every outgoing request, if one exists. */
class AuthInterceptor @Inject constructor(
    private val sessionStore: SessionStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val session = sessionStore.getSession()
        val request = if (session != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer ${session.accessToken}")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
