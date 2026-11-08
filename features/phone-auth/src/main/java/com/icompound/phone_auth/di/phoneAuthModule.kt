package com.icompound.phone_auth.di

import org.koin.dsl.module

val phoneAuthModule = module {
    includes(
        phoneAuthDomainModule,
        phoneAuthPresentationModule,
        phoneAuthDataModule
    )
}