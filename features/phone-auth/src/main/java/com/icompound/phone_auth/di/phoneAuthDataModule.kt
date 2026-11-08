package com.icompound.phone_auth.di

import com.icompound.phone_auth.data.mapper.ManagerContactMapper
import com.icompound.phone_auth.data.repo.PhoneAuthRepository
import com.icompound.phone_auth.domain.repo.IPhoneAuthRepository
import org.koin.dsl.module

val phoneAuthDataModule = module {

    factory {
        ManagerContactMapper()
    }

    single<IPhoneAuthRepository> {
        PhoneAuthRepository(
            network = get(),
            managerContactMapper = get(),
        )
    }
}