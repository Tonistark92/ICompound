package com.icompound.phoneauth.domain.repo

import com.icompound.core.domain.model.request.RemoteRequest
import com.icompound.phoneauth.domain.model.ManagerContact

interface IPhoneAuthRepository {
    suspend fun getManagerContact(remoteRequest: RemoteRequest): ManagerContact
}
