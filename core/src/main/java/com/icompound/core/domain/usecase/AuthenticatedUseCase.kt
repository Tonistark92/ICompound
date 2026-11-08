package com.icompound.core.domain.usecase


import com.icompound.core.domain.model.User
import com.icompound.core.domain.model.error.ErrorHandler
import com.icompound.core.domain.repo.local.IPreferenceRepository
import com.icompound.core.presentation.base.DispatcherProvider
import com.icompound.core.util.toJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Base for every use case that runs AFTER login and needs the user id.
 * One line in the use case (`userId()`), but the dependency is a normal constructor parameter:
 * no KoinComponent, no runBlocking, easy to fake in tests.
 *
 * Use plain BaseUseCase for pre-login calls (manager contact, send OTP) and for the use case that creates the session.
 */
abstract class AuthenticatedUseCase<Domain, in Body>(
    errorHandler: ErrorHandler,
    dispatchers: DispatcherProvider,
) : BaseUseCase<Domain, Body>(errorHandler, dispatchers) {

    protected open val preferenceRepository: IPreferenceRepository by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        getKoin().get<IPreferenceRepository>()
    }

    protected fun saveUser(user: User) {
        runBlocking {
            preferenceRepository.setUserData(user.toJson())
            preferenceRepository.setToken(user.token ?: "")
            preferenceRepository.setUserId(user.id.toString())
            preferenceRepository.setIsLogin(true)
        }
    }

    protected suspend fun getUserId(): String {
        return preferenceRepository.getUserId().first()
    }
    protected suspend fun saveUserId(userId: String) {
        preferenceRepository.setUserId(userId)
    }
    protected suspend fun getUserToken(): String {
        return preferenceRepository.getToken().first()
    }

}

/*
 * class GetMyBillsUC(
 *     private val repo: IBillingRepo,
 *     session: ISessionRepository,
 *     errorHandler: ErrorHandler,
 *     dispatchers: DispatcherProvider,
 * ) : AuthenticatedUseCase<Bills, GetBillsParams>(session, errorHandler, dispatchers) {
 *
 *     override fun executeDS(body: GetBillsParams?): Flow<Bills> = flow {
 *         val params = requireBody(body)
 *         val request = GetBillsRequest(userId = userId(), page = params.page)
 *         request.validateRequestContract()
 *         emit(repo.getBills(request.remoteMap))
 *     }
 * }
 */