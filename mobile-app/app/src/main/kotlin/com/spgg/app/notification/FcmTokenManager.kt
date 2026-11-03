package com.spgg.app.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.spgg.app.data.local.SessionStore
import com.spgg.app.data.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FcmTokenManager @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val sessionStore: SessionStore
) {

    suspend fun registerCurrentTokenIfLoggedIn() {
        if (sessionStore.getSession() == null) return
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            notificationRepository.registerToken(token)
        }
    }

    suspend fun registerToken(token: String) {
        if (sessionStore.getSession() == null) return
        runCatching { notificationRepository.registerToken(token) }
    }

    fun canPostNotifications(context: android.content.Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }
}
