package com.icompound

import android.app.Application
import com.icompound.core.di.coroutineModule
import com.icompound.core.di.iCompoundCoreModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@BaseApp)

            modules(
                iCompoundCoreModule,
//                appModule
                // featuresModule, when you have feature modules
            )
        }
    }
}