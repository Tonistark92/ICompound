package com.icompound.phone_auth.domain

sealed interface NumberCheck {

    data object Registered : NumberCheck

    data object NotRegistered : NumberCheck

    data class Invalid(
        val message: String
    ) : NumberCheck
}