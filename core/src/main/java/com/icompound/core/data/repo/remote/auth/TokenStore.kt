package com.icompound.core.data.repo.remote.auth

import com.icompound.core.domain.repo.local.IPreferenceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

interface TokenStore {
    val accessToken: String?
    val refreshToken: String?

    fun save(
        access: String,
        refresh: String?,
    )

    fun clear()
}

class DefaultTokenStore(
    private val preferenceRepository: IPreferenceRepository,
) : TokenStore {
    private var _accessToken: String? = null
    private var _refreshToken: String? = null

    override val accessToken: String?
        get() =
            _accessToken ?: runBlocking {
                preferenceRepository
                    .getToken()
                    .firstOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.also { _accessToken = it }
            }

    override val refreshToken: String?
        get() = _refreshToken

    override fun save(
        access: String,
        refresh: String?,
    ) {
        _accessToken = access
        _refreshToken = refresh
        runBlocking {
            preferenceRepository.setToken(access)
        }
    }

    override fun clear() {
        _accessToken = null
        _refreshToken = null
        runBlocking {
            preferenceRepository.onLogout()
        }
    }
}

/** Lets the network layer tell the app "go back to login" without depending on UI. */
class SessionEvents {
    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expired: SharedFlow<Unit> = _expired

    fun notifyExpired() {
        _expired.tryEmit(Unit)
    }
}
