package com.icompound.core.domain.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep

@Keep
@Serializable
data class PhoneNumber(
    @SerialName("countryCode")
    val countryCode: String? = "", // EG
    @SerialName("dialCode")
    val dialCode: String? = "", // +20
    @SerialName("e164Number")
    val e164Number: String? = "", // +201010933063
    @SerialName("internationalNumber")
    val internationalNumber: String? = "", // +20 10 10933063
    @SerialName("nationalNumber")
    val nationalNumber: String? = "", // 010 10933063
    @SerialName("number")
    val number: String? = "" // 01010933063
)