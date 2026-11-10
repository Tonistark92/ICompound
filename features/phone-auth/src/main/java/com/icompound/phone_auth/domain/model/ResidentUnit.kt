package com.icompound.phone_auth.domain.model

/** A unit the resident can pick on screen 1.5 */
data class ResidentUnit(
    val id: String,
    val title: String,        // "Villa 14"
    val subtitle: String? = null,
)
