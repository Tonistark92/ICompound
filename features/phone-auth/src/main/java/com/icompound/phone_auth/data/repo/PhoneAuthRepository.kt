package com.icompound.phone_auth.data.repo

import com.icompound.core.domain.model.request.RemoteRequest
import com.icompound.core.domain.repo.remote.INetworkProvider
import com.icompound.phone_auth.data.mapper.ManagerContactMapper
import com.icompound.phone_auth.data.model.dto.ManagerContactDto
import com.icompound.phone_auth.data.model.dto.ManagerContactResponseDto
import com.icompound.phone_auth.domain.ManagerContact
import com.icompound.phone_auth.domain.repo.IPhoneAuthRepository
import kotlin.jvm.java

class PhoneAuthRepository(
    private val network: INetworkProvider,
    private val managerContactMapper: ManagerContactMapper,
) : IPhoneAuthRepository {

    override suspend fun getManagerContact(request: RemoteRequest): ManagerContact {
        val dto = network.get<ManagerContactResponseDto>(
            responseWrappedModel = ManagerContactResponseDto::class.java,
            pathUrl = MANAGER_CONTACT_PATH,
            headers = request.requestHeaders,
            queryParams = request.requestQueries,
        )
        return managerContactMapper.dtoToDomain(dto)
    }

    private companion object {
        // PLACEHOLDER: replace with the real path from the EyeCompound OpenAPI spec (no leading slash).
        const val MANAGER_CONTACT_PATH = "manager/contact"
    }
}
