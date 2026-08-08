package com.spgg.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spgg.app.BuildConfig
import com.spgg.app.data.remote.AuthApi
import com.spgg.app.data.remote.AuthInterceptor
import com.spgg.app.data.remote.RawAuthApi
import com.spgg.app.data.remote.RawOkHttpClient
import com.spgg.app.data.remote.BottleApi
import com.spgg.app.data.remote.CustomerApi
import com.spgg.app.data.remote.LedgerApi
import com.spgg.app.data.remote.OrderApi
import com.spgg.app.data.remote.PaymentApi
import com.spgg.app.data.remote.StaffApi
import com.spgg.app.data.remote.ProductApi
import com.spgg.app.data.remote.RawRetrofit
import com.spgg.app.data.remote.ShopProfileApi
import com.spgg.app.data.remote.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // "Raw" client/retrofit/api — no interceptor, no authenticator. Used only by
    // TokenAuthenticator to call /auth/refresh without recursing into itself.
    @Provides
    @Singleton
    @RawOkHttpClient
    fun provideRawOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    @RawRetrofit
    fun provideRawRetrofit(@RawOkHttpClient client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @RawAuthApi
    fun provideRawAuthApi(@RawRetrofit retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideShopProfileApi(retrofit: Retrofit): ShopProfileApi = retrofit.create(ShopProfileApi::class.java)

    @Provides
    @Singleton
    fun provideProductApi(retrofit: Retrofit): ProductApi = retrofit.create(ProductApi::class.java)

    @Provides
    @Singleton
    fun provideCustomerApi(retrofit: Retrofit): CustomerApi = retrofit.create(CustomerApi::class.java)

    @Provides
    @Singleton
    fun provideOrderApi(retrofit: Retrofit): OrderApi = retrofit.create(OrderApi::class.java)

    @Provides
    @Singleton
    fun provideLedgerApi(retrofit: Retrofit): LedgerApi = retrofit.create(LedgerApi::class.java)

    @Provides
    @Singleton
    fun providePaymentApi(retrofit: Retrofit): PaymentApi = retrofit.create(PaymentApi::class.java)

    @Provides
    @Singleton
    fun provideStaffApi(retrofit: Retrofit): StaffApi = retrofit.create(StaffApi::class.java)

    @Provides
    @Singleton
    fun provideBottleApi(retrofit: Retrofit): BottleApi = retrofit.create(BottleApi::class.java)
}
