package com.icompound.core.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icompound.core.domain.model.User
import com.icompound.core.domain.repo.local.IPreferenceRepository
import com.icompound.core.util.toJson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import kotlin.getValue
import kotlin.jvm.java

abstract class BaseViewModel(
    protected val dispatcherProvider: DispatcherProvider,
) : ViewModel(),
    CoroutineExecutor {
    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                onError(throwable)
            }
        }

    protected val viewModelScopeWithHandler: CoroutineScope
        get() = viewModelScope + exceptionHandler

    override val coroutineScope: CoroutineScope
        get() = viewModelScopeWithHandler

    // ------------------------------------------------------------------------
    // Coroutine Execution
    // ------------------------------------------------------------------------

    protected fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
        launchCoroutine(
            dispatcher = dispatcherProvider.io,
            block = block,
        )

    protected fun launchDefault(block: suspend CoroutineScope.() -> Unit): Job =
        launchCoroutine(
            dispatcher = dispatcherProvider.default,
            block = block,
        )

    // ------------------------------------------------------------------------
    // Error Handling
    // ------------------------------------------------------------------------

    /**
     * Handles uncaught coroutine exceptions.
     *
     * Override in the application or architecture-specific base
     * when a shared error-handling strategy is required.
     */
    protected open fun onError(throwable: Throwable) {
        // TODO: Connect to the ICompound logger / error handler.
    }
}
