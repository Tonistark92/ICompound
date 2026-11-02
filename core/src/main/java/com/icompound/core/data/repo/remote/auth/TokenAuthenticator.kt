package com.icompound.core.data.repo.remote.auth

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val refreshApi: AuthRefreshApi,
    private val sessionEvents: SessionEvents,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Requests that must never be retried with a refreshed token (login / refresh itself).
        if (response.request.header(NO_AUTH_HEADER) != null) return null
        // Give up after one retry to avoid infinite loops.
        if (response.responseCount() >= 2) return expire()

        synchronized(this) {
            val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()
            val currentToken = tokenStore.accessToken

            // Another request already refreshed the token while we were waiting for the lock.
            if (!currentToken.isNullOrBlank() && currentToken != sentToken) {
                return response.request.withToken(currentToken)
            }

            val refreshToken = tokenStore.refreshToken ?: return expire()
            val refreshed = runCatching {
                refreshApi.refresh(RefreshRequest(refreshToken)).execute()
            }.getOrNull()?.takeIf { it.isSuccessful }?.body() ?: return expire()

            tokenStore.save(refreshed.accessToken, refreshed.refreshToken ?: refreshToken)
            return response.request.withToken(refreshed.accessToken)
        }
    }

    private fun expire(): Request? {
        tokenStore.clear()
        sessionEvents.notifyExpired()
        return null
    }

    private fun Request.withToken(token: String) =
        newBuilder().header("Authorization", "Bearer $token").build()

    private fun Response.responseCount(): Int {
        var count = 0
        var r: Response? = this
        while (r != null) { count++; r = r.priorResponse }
        return count
    }

    companion object { const val NO_AUTH_HEADER = "X-No-Auth" }
}