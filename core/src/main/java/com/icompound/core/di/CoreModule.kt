package com.icompound.core.di

import com.icompound.core.domain.model.error.ErrorHandler
import com.icompound.core.domain.model.error.GeneralErrorHandlerImpl
import org.koin.dsl.module


val iCompoundCoreModule = module {

    includes(
        networkModule,
        coroutineModule
    )

    single<ErrorHandler> {
        GeneralErrorHandlerImpl()
    }
}