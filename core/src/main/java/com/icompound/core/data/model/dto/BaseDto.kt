package com.icompound.core.data.model.dto

import com.google.gson.annotations.SerializedName

open class BaseDto(
    @SerializedName("message") var message: String? = null,
    @SerializedName("code") var code: String? = null
)