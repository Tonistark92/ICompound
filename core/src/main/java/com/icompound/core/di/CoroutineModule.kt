package com.icompound.core.di

import com.icompound.core.presentation.base.DefaultDispatcherProvider
import com.icompound.core.presentation.base.DispatcherProvider
import org.koin.dsl.module

val coroutineModule = module {

    single<DispatcherProvider> {
        DefaultDispatcherProvider()
    }
}