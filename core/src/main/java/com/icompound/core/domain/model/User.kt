package com.icompound.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("code")
    val code: Int? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("firstName")
    val firstName: String? = null,
    @SerialName("middleName")
    val middleName: String? = null,
    @SerialName("lastName")
    val lastName: String? = null,
    @SerialName("id")
    val id: String? = null,
    @SerialName("token")
    val token: String? = null,
    @SerialName("username")
    val username: String? = null,
    @SerialName("phoneNumber")
    val phoneNumber: PhoneNumber? = null,
    @SerialName("whatsappNumber")
    val whatsappNumber: WhatsappNumber? = null,
    @SerialName("isActive")
    val isActive: Boolean? = null,
    @SerialName("isDeactivated")
    val isDeactivated: Boolean? = null,
    @SerialName("deactivatedAt")
    val deactivatedAt: String? = null,
    @SerialName("profilePicture")
    val profilePicture: String? = null,
    @SerialName("hasProfilePhoto")
    val hasProfilePhoto: Boolean? = null,
)
