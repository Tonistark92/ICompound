package com.icompound.phone_auth.data.repo

import com.icompound.core.domain.model.request.RemoteRequest
import com.icompound.core.domain.repo.remote.INetworkProvider
import com.icompound.phone_auth.data.mapper.ManagerContactMapper
import com.icompound.phone_auth.data.model.dto.ManagerContactResponseDto
import com.icompound.phone_auth.domain.model.ManagerContact
import com.icompound.phone_auth.domain.repo.IPhoneAuthRepository
import kotlin.jvm.java

class PhoneAuthRepository(
    private val network: INetworkProvider,
    private val managerContactMapper: ManagerContactMapper,
) : IPhoneAuthRepository {

    override suspend fun getManagerContact(remoteRequest: RemoteRequest): ManagerContact {
        val dto = network.get<ManagerContactResponseDto>(
            responseWrappedModel = ManagerContactResponseDto::class.java,
            pathUrl = MANAGER_CONTACT_PATH,
            headers = remoteRequest.requestHeaders,
            queryParams = remoteRequest.requestQueries,
        )
        return managerContactMapper.dtoToDomain(dto)
    }

    private companion object {
        // PLACEHOLDER: replace with the real path from the EyeCompound OpenAPI spec (no leading slash).
        const val MANAGER_CONTACT_PATH = "manager/contact"
    }
}
