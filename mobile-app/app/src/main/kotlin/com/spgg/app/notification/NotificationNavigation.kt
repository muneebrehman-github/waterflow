package com.spgg.app.notification

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Holds a pending order deep-link from a notification tap until navigation is ready. */
object NotificationNavigation {
    const val EXTRA_ORDER_ID = "orderId"

    private val _pendingOrderId = MutableStateFlow<Long?>(null)
    val pendingOrderId: StateFlow<Long?> = _pendingOrderId.asStateFlow()

    fun setFromIntent(intent: Intent?) {
        val orderId = intent?.getStringExtra(EXTRA_ORDER_ID)?.toLongOrNull() ?: return
        _pendingOrderId.value = orderId
    }

    fun consume(): Long? {
        val current = _pendingOrderId.value
        _pendingOrderId.value = null
        return current
    }
}
