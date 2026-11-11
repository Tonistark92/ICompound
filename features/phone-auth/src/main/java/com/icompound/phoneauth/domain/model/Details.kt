package com.icompound.phoneauth.domain.model

/** Data shown on the "Review your details" screen (1.6) */
data class Details(
    val fullName: String,
    val maskedMobile: String, // "+20 • • • • 4567"
    val unit: ResidentUnit,
    val allowNotifications: Boolean = true,
)
