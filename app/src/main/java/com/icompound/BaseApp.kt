package com.icompound

import android.app.Application
import com.icompound.core.di.iCompoundCoreModule
import com.icompound.di.featuresModule
import com.icompound.navigation.di.navigationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@BaseApp)

            modules(
                iCompoundCoreModule,
                featuresModule,
                navigationModule,
            )
        }
    }
}