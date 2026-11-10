package com.icompound.phone_auth.domain.repo

import com.icompound.core.domain.model.request.RemoteRequest
import com.icompound.phone_auth.domain.model.ManagerContact


interface IPhoneAuthRepository {

    suspend fun getManagerContact(
        remoteRequest: RemoteRequest
    ): ManagerContact
}