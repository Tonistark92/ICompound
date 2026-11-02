package com.icompound.core.presentation.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface CoroutineExecutor {

    val coroutineScope: CoroutineScope

    fun launchCoroutine(
        dispatcher: CoroutineDispatcher? = null,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return coroutineScope.launch(
            context = dispatcher ?: kotlin.coroutines.EmptyCoroutineContext,
            start = start,
            block = block
        )
    }
}