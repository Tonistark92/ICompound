package com.icompound.core.data.repo.remote.auth

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthRefreshApi {
    // PLACEHOLDER: replace path / body / response with the real Icompound backend contract.
    @POST("auth/refresh")
    fun refresh(
        @Body body: RefreshRequest,
    ): Call<RefreshResponse>
}

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String,
)

data class RefreshResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
)
