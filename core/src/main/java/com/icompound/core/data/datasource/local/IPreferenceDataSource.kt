package com.icompound.core.data.datasource.local

import kotlinx.coroutines.flow.Flow

interface IPreferenceDataSource {
    suspend fun getValue(key: String, default: Any?): Flow<Any?>
    suspend fun setValue(key: String, value: Any?)
}