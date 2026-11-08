package com.icompound.phone_auth.domain.usecase

import com.icompound.core.domain.model.error.ErrorHandler
import com.icompound.core.domain.model.request.RemoteRequest
import com.icompound.core.domain.usecase.BaseUseCase
import com.icompound.core.presentation.base.DispatcherProvider
import com.icompound.phone_auth.domain.ManagerContact
import com.icompound.phone_auth.domain.repo.IPhoneAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetManagerContactUC(
    private val repo: IPhoneAuthRepository,
    errorHandler: ErrorHandler,
    dispatchers: DispatcherProvider,
) : BaseUseCase<ManagerContact, RemoteRequest>(
    errorHandler,
    dispatchers,
) {

    override fun executeDS(
        body: RemoteRequest?
    ): Flow<ManagerContact> = flow {
        val request = body ?: RemoteRequest()

        emit(repo.getManagerContact(request))
    }
}