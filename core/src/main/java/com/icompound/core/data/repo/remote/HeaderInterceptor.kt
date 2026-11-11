package com.icompound.core.data.repo.remote

import com.icompound.core.data.repo.remote.auth.TokenStore
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class HeaderInterceptor(
    private val tokenStore: TokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val token = tokenStore.accessToken
        val requestBuilder =
            original.newBuilder().apply {
                if (!token.isNullOrBlank() && original.header(AUTHORIZATION) == null) {
                    addHeader(AUTHORIZATION, "Bearer $token")
                }
                addHeader(ACCEPT, APP_JSON)
                addHeader(CONTENT_TYPE, APP_JSON)
            }
        return chain.proceed(requestBuilder.build())
    }

    companion object {
        private const val AUTHORIZATION = "Authorization"
        private const val ACCEPT = "Accept"
        private const val CONTENT_TYPE = "Content-Type"
        private const val APP_JSON = "application/json"
    }
}
