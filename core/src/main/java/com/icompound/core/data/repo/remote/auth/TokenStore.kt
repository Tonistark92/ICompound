package com.icompound.core.data.repo.remote.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface TokenStore {
    val accessToken: String?
    val refreshToken: String?
    fun save(access: String, refresh: String?)
    fun clear()
}
// Implement with your KVault module.

/** Lets the network layer tell the app "go back to login" without depending on UI. */
class SessionEvents {
    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expired: SharedFlow<Unit> = _expired
    fun notifyExpired() { _expired.tryEmit(Unit) }
}