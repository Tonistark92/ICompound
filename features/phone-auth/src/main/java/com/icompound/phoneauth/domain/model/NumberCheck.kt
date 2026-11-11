package com.icompound.phoneauth.domain.model

sealed interface NumberCheck {
    data object Registered : NumberCheck

    data object NotRegistered : NumberCheck

    data class Invalid(
        val message: String,
    ) : NumberCheck
}
