package com.icompound.core.domain.usecase

import com.icompound.core.data.model.Resource
import com.icompound.core.domain.model.error.ErrorHandler
import com.icompound.core.presentation.base.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

abstract class BaseUseCase<Domain, in Body>(
    private val errorHandler: ErrorHandler,
    private val dispatchers: DispatcherProvider,
) {
    protected fun requireBody(body: Body?): @UnsafeVariance Body =
        body ?: throw IllegalArgumentException("UseCase body is required and was null")

    protected abstract fun executeDS(body: Body? = null): Flow<Domain>

    // Callback style (used from ViewModels)
    operator fun invoke(
        scope: CoroutineScope,
        body: Body? = null,
        multipleInvoke: Boolean = false,
        onResult: (Resource<Domain>) -> Unit,
    ) {
        scope.launch(dispatchers.main) {
            if (!multipleInvoke) onResult(Resource.loading())
            runFlow(executeDS(body), onResult).collect {
                onResult(Resource.success(it))
                if (!multipleInvoke) onResult(Resource.loading(false))
            }
        }
    }

    // Flow style (easier to test / compose)
    operator fun invoke(
        body: Body? = null,
        multipleInvoke: Boolean = false,
    ): Flow<Resource<Domain>> = channelFlow {
        if (!multipleInvoke) send(Resource.loading())
        runFlow(executeDS(body)) { send(it) }.collect {
            send(Resource.success(it))
            if (!multipleInvoke) send(Resource.loading(false))
        }
    }

    private fun <M> runFlow(
        requestExecution: Flow<M>,
        onResult: suspend (Resource<Domain>) -> Unit,
    ): Flow<M> = requestExecution
        .catch { e ->
            onResult(Resource.failure(errorHandler.getError(e)))
            onResult(Resource.loading(false))
        }
        .flowOn(dispatchers.io)
}