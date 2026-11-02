package com.icompound.core.data.repo.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class HeaderInterceptor(
//    private val headerInterceptorUC: GetUserTokenUC
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val requestBuilder = original.newBuilder().apply {
            runBlocking {
//                val token = getUserAccessToken()
//
//                if (token.isNotEmpty()) {
//                    addHeader(Constants.AUTHORIZATION, token)
//                }
            }

            addHeader(ACCEPT, APP_JSON)
            addHeader(CONTENT_TYPE, APP_JSON)
        }
        return chain.proceed(requestBuilder.build())
    }

//    private suspend fun getUserAccessToken(): String {
//        var accessToken = ""
//        headerInterceptorUC.invoke().collect {
//            accessToken = when (it) {
//                is Resource.Failure -> {
//                    Log.e(
//                        "headerInterceptor",
//                        "Can't catch user access token , error occur with: ${it.exception.message}"
//                    )
//                    ""
//                }
//
//                is Resource.Progress -> return@collect
//                is Resource.Success -> it.model
//            }
//        }
//        return "Bearer $accessToken"
//    }

    companion object {
        private const val ACCEPT = "Accept"
        private const val CONTENT_TYPE = "Content-Type"
        private const val APP_JSON = "application/json"
    }
}