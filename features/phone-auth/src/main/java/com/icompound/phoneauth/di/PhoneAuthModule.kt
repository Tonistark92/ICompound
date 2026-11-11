package com.icompound.phoneauth.di

import org.koin.dsl.module

val phoneAuthModule =
    module {
        includes(
            phoneAuthDomainModule,
            phoneAuthPresentationModule,
            phoneAuthDataModule,
        )
    }
