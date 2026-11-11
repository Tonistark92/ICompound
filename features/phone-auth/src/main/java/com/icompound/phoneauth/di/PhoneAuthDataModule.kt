package com.icompound.phoneauth.di

import com.icompound.phoneauth.data.mapper.ManagerContactMapper
import com.icompound.phoneauth.data.repo.PhoneAuthRepository
import com.icompound.phoneauth.domain.repo.IPhoneAuthRepository
import org.koin.dsl.module

val phoneAuthDataModule =
    module {

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
