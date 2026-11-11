package com.icompound.phoneauth.data.repo

import com.icompound.core.domain.model.request.RemoteRequest
import com.icompound.core.domain.repo.remote.INetworkProvider
import com.icompound.phoneauth.data.mapper.ManagerContactMapper
import com.icompound.phoneauth.data.model.dto.ManagerContactResponseDto
import com.icompound.phoneauth.domain.model.ManagerContact
import com.icompound.phoneauth.domain.repo.IPhoneAuthRepository

class PhoneAuthRepository(
    private val network: INetworkProvider,
    private val managerContactMapper: ManagerContactMapper,
) : IPhoneAuthRepository {
    override suspend fun getManagerContact(remoteRequest: RemoteRequest): ManagerContact {
        val dto =
            network.get<ManagerContactResponseDto>(
                responseWrappedModel = ManagerContactResponseDto::class.java,
                pathUrl = MANAGER_CONTACT_PATH,
                headers = remoteRequest.requestHeaders,
                queryParams = remoteRequest.requestQueries,
            )
        return managerContactMapper.dtoToDomain(dto)
    }

    private companion object {
        const val MANAGER_CONTACT_PATH = "manager/contact"
    }
}
