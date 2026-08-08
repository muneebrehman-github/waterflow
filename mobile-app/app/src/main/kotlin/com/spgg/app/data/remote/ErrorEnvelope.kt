package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

/** Minimal shape used to pull just the message out of an error response body,
 *  ignoring the "data"/"timestamp" fields we don't care about here. */
@Serializable
data class ErrorEnvelope(val success: Boolean = false, val message: String? = null)
