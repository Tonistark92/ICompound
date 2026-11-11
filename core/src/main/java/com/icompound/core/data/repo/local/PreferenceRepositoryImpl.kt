package com.icompound.core.data.repo.local

import com.icompound.core.data.datasource.local.IPreferenceDataSource
import com.icompound.core.domain.repo.local.IPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PreferenceRepositoryImpl(
    private val preferenceDataSource: IPreferenceDataSource,
) : IPreferenceRepository {
    override suspend fun getToken() =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.TOKEN, "").collect {
                emit(it as String)
            }
        }

    override suspend fun setToken(mToken: String) {
        preferenceDataSource.setValue(PreferenceConstants.TOKEN, mToken)
    }

    override suspend fun getUserId(): Flow<String> =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.USER_ID, "").collect {
                emit(it as String)
            }
        }

    override suspend fun setUserId(userId: String) {
        preferenceDataSource.setValue(PreferenceConstants.USER_ID, userId)
    }

    override suspend fun getNotifyStatus() =
        flow {
            preferenceDataSource.getValue("notify_status", "true").collect {
                emit(it as String)
            }
        }

    override suspend fun setNotifyStatus(status: String) {
        preferenceDataSource.setValue("notify_status", status)
    }

    override suspend fun getUserData() =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.USER_DATA, "").collect {
                emit(it as String)
            }
        }

    override suspend fun setUserData(userData: String) {
        preferenceDataSource.setValue(PreferenceConstants.USER_DATA, userData)
    }

    override suspend fun getSocketData() =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.SOCKET_DATA, "").collect {
                emit(it as String)
            }
        }

    override suspend fun setSocketData(data: String) {
        preferenceDataSource.setValue(PreferenceConstants.SOCKET_DATA, data)
    }

    override suspend fun getFirebaseToken() =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.FIREBASE_TOKEN, "").collect {
                emit(it as String)
            }
        }

    override suspend fun setFirebaseToken(token: String) {
        preferenceDataSource.setValue(PreferenceConstants.FIREBASE_TOKEN, token)
    }

    override suspend fun getAvailabilityStatus() =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.AVAILABILITY, true).collect {
                emit(it as Boolean)
            }
        }

    override suspend fun setAvailabilityStatus(status: Boolean) {
        preferenceDataSource.setValue(PreferenceConstants.AVAILABILITY, status)
    }

    override suspend fun getUserType() =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.USER_TYPE, "user").collect {
                emit(it as String)
            }
        }

    override suspend fun setUserType(type: String) {
        preferenceDataSource.setValue(PreferenceConstants.USER_TYPE, type)
    }

    override suspend fun getIsFirstTime() =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.IS_FIRST_TIME, true).collect {
                emit(it as Boolean)
            }
        }

    override suspend fun setIsFirstTime(firstTime: Boolean) {
        preferenceDataSource.setValue(PreferenceConstants.IS_FIRST_TIME, firstTime)
    }

    override suspend fun getIsLogin() =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.IS_LOGIN, false).collect {
                emit(it as Boolean)
            }
        }

    override suspend fun setIsLogin(isLogin: Boolean) {
        preferenceDataSource.setValue(PreferenceConstants.IS_LOGIN, isLogin)
    }

    override suspend fun getCountries() =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.COUNTRIES, "").collect {
                emit(it as String)
            }
        }

    override suspend fun setCountries(countries: String) {
        preferenceDataSource.setValue(PreferenceConstants.COUNTRIES, countries)
    }

    override suspend fun getCertificateNamePromptCheckedAt() =
        flow {
            preferenceDataSource.getValue(PreferenceConstants.CERTIFICATE_NAME_PROMPT_CHECKED_AT, 0L).collect {
                emit(it as Long)
            }
        }

    override suspend fun setCertificateNamePromptCheckedAt(timestamp: Long) {
        preferenceDataSource.setValue(PreferenceConstants.CERTIFICATE_NAME_PROMPT_CHECKED_AT, timestamp)
    }

    override suspend fun onLogout() {
        setToken("")
        setUserData("")
        setUserId("")
        setSocketData("")
        setIsLogin(false)
        setCertificateNamePromptCheckedAt(0L)
    }
}
