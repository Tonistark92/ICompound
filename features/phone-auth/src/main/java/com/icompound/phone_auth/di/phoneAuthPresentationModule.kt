package com.icompound.phone_auth.di

import com.icompound.core.presentation.base.DispatcherProvider
import com.icompound.phone_auth.domain.usecase.GetManagerContactUC
import com.icompound.phone_auth.presentation.PhoneAuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val phoneAuthPresentationModule = module {

    viewModel {
        PhoneAuthViewModel(
            getManagerContact = get<GetManagerContactUC>(),
//            checkNumber = get<CheckNumberUseCase>(),
//            sendCode = get<SendCodeUseCase>(),
//            verifyCode = get<VerifyOTPCodeUseCase>(),
            dispatcherProvider = get<DispatcherProvider>(),
        )
    }
}