package com.icompound.di

import com.icompound.core.presentation.utils.UiEventManager
import com.icompound.mainactivity.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featuresModule = module {
    single {
        UiEventManager()
    }

    viewModel {
        MainViewModel(
            uiEventManager = get()
        )
    }
//    includes(
//        loginModule,
//        profileModule
//    )


//    single {
//        AppConfig(
//            baseUrl = BuildConfig.BASE_URL,
//            isDebug = BuildConfig.DEBUG
//        )
//    }
}

//if you need BuildConfig values through Koin
//data class AppConfig(
//    val baseUrl: String,
//    val isDebug: Boolean
//)