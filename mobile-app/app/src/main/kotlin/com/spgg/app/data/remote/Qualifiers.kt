package com.spgg.app.data.remote

import javax.inject.Qualifier

/** OkHttpClient/Retrofit/AuthApi with no interceptor or authenticator attached —
 *  used only by [TokenAuthenticator] to call the refresh endpoint without recursing. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RawOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RawRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RawAuthApi
