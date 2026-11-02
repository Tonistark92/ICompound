package com.icompound.core.di

import android.R.attr.level
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.icompound.core.BuildConfig
import com.icompound.core.data.repo.remote.ApiService
import com.icompound.core.data.repo.remote.HeaderInterceptor
import com.icompound.core.data.repo.remote.RetrofitNetworkProvider
import com.icompound.core.data.repo.remote.auth.AuthRefreshApi
import com.icompound.core.data.repo.remote.auth.TokenAuthenticator
import com.icompound.core.domain.repo.remote.INetworkProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

private const val REFRESH = "refresh"

internal val networkModule = module {
    single<Gson> {
        GsonBuilder().create()
    }

    single<ApiService> {
        get<Retrofit>().create(ApiService::class.java)
    }

    single<INetworkProvider> {
        RetrofitNetworkProvider(get())
    }
    single<OkHttpClient>(named(REFRESH)) { baseClient(androidContext()).build() }
    single<AuthRefreshApi> {
        Retrofit.Builder()
            .client(get(named(REFRESH)))
            .baseUrl(BuildConfig.REMOTE_URL)
            .addConverterFactory(
                get<Json>().asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
            .create(AuthRefreshApi::class.java)
    }
    single { TokenAuthenticator(get(), get(), get()) }

    single<Interceptor> {
        HeaderInterceptor()
    }

    single<OkHttpClient> {
        baseClient(androidContext())
            .addInterceptor(HeaderInterceptor())      // your existing one
            .authenticator(get<TokenAuthenticator>())
            .build()
    }

    single<Cache> {
        Cache(File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()), 1024)
    }

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.REMOTE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(
                get<Json>().asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
    }
}

private fun getHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
    else HttpLoggingInterceptor.Level.NONE
}

private fun baseClient(context: Context): OkHttpClient.Builder =
    OkHttpClient.Builder()
        .cache(Cache(File(context.cacheDir, "http_cache"), 10L * 1024 * 1024))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                    redactHeader("Authorization")
                    redactHeader("Cookie")
                })
            } else {
//                certificatePinner(certificatePinner())   // pinning only in release
            }
        }