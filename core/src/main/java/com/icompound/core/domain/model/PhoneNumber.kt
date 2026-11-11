package com.icompound.core.domain.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class PhoneNumber(
    @SerialName("countryCode")
    val countryCode: String? = "",
    @SerialName("dialCode")
    val dialCode: String? = "",
    @SerialName("e164Number")
    val e164Number: String? = "",
    @SerialName("internationalNumber")
    val internationalNumber: String? = "",
    @SerialName("nationalNumber")
    val nationalNumber: String? = "",
    @SerialName("number")
    val number: String? = "",
)
