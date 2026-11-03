package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceTokenRequestDto(val token: String)
