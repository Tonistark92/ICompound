package com.icompound.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.icompound.core.BuildConfig
import com.icompound.core.data.datasource.local.IPreferenceDataSource
import com.icompound.core.data.datasource.local.PreferenceDataSource
import com.icompound.core.data.repo.local.PreferenceRepositoryImpl
import com.icompound.core.domain.model.error.ErrorHandler
import com.icompound.core.domain.model.error.GeneralErrorHandlerImpl
import com.icompound.core.domain.repo.local.IPreferenceRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val iCompoundCoreModule = module {

    includes(
        networkModule,
        coroutineModule
    )

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = {
                androidContext().preferencesDataStoreFile(
                    BuildConfig.IC_COMPOUND_DATASTORE_NAME
                )
            }
        )
    }
    single<IPreferenceRepository> {
        PreferenceRepositoryImpl(
            preferenceDataSource = get()
        )
    }

    single<IPreferenceDataSource> {
        PreferenceDataSource(
            dataStore = get()
        )
    }

    single<ErrorHandler> {
        GeneralErrorHandlerImpl()
    }
}