package com.spgg.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.spgg.app.domain.AuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Persists the active session (tokens + role/tenant metadata) in the Android Keystore-backed
 *  EncryptedSharedPreferences, so the app can restore login state across restarts. */
@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json
) {
    private val prefs = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "spgg_secure_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getSession(): AuthSession? {
        val raw = prefs.getString(KEY_SESSION, null) ?: return null
        return runCatching { json.decodeFromString(AuthSession.serializer(), raw) }.getOrNull()
    }

    fun saveSession(session: AuthSession) {
        prefs.edit().putString(KEY_SESSION, json.encodeToString(session)).apply()
    }

    fun updateAccessToken(accessToken: String) {
        getSession()?.let { saveSession(it.copy(accessToken = accessToken)) }
    }

    fun clearSession() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    private companion object {
        const val KEY_SESSION = "session"
    }
}
