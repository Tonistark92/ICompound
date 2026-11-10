package com.icompound.phone_auth.domain.usecase

import com.icompound.core.domain.model.error.ErrorHandler
import com.icompound.core.domain.usecase.BaseUseCase
import com.icompound.core.presentation.base.DispatcherProvider
import com.icompound.phone_auth.data.repo.PhoneAuthRepository
import com.icompound.phone_auth.domain.model.GetManagerContactRequest
import com.icompound.phone_auth.domain.model.ManagerContact
import com.icompound.phone_auth.domain.repo.IPhoneAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetManagerContactUC(
    private val repo: IPhoneAuthRepository,
    errorHandler: ErrorHandler,
    dispatchers: DispatcherProvider,
) : BaseUseCase<ManagerContact, GetManagerContactRequest>(errorHandler, dispatchers) {

    override fun executeDS(body: GetManagerContactRequest?): Flow<ManagerContact> = flow {
        val request = body ?: GetManagerContactRequest()   // no params, so a default is safe
        request.validateRequestContract()
        emit(repo.getManagerContact(request.remoteMap))
    }
}