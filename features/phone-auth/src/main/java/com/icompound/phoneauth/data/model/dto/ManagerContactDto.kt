package com.icompound.phoneauth.data.model.dto

import com.google.gson.annotations.SerializedName
import com.icompound.core.data.model.dto.BaseDto

/**
 * PLACEHOLDER field names – align with the EyeCompound OpenAPI spec for the manager-contact endpoint.
 * Every field is nullable with a default: Gson ignores Kotlin null-safety.
 */
data class ManagerContactDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
)

/**
 * Response envelope: { "data": { ... }, "message": "...", "code": "..." }.
 * message / code come from BaseDto. If the real response is flat (no "data"), delete this wrapper
 * and make ManagerContactDto extend BaseDto instead.
 */
data class ManagerContactResponseDto(
    @SerializedName("data") val data: ManagerContactDto? = null,
) : BaseDto()
