package com.icompound.phone_auth.di

import com.icompound.core.domain.model.error.ErrorHandler
import com.icompound.core.presentation.base.DispatcherProvider
import com.icompound.phone_auth.domain.repo.IPhoneAuthRepository
import com.icompound.phone_auth.domain.usecase.GetManagerContactUC
import org.koin.dsl.module

val phoneAuthDomainModule = module {
//
//    factory { CheckNumberUseCase(get()) }
//
//    factory { SendCodeUseCase(get()) }
//
//    factory { VerifyOTPCodeUseCase(get()) }

    factory {
        GetManagerContactUC(
            repo = get<IPhoneAuthRepository>(),
            errorHandler = get<ErrorHandler>(),
            dispatchers = get<DispatcherProvider>(),
        )
    }
}