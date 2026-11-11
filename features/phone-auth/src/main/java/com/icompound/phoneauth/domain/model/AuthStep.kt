package com.icompound.phoneauth.domain.model

sealed interface AuthStep {
    data class Phone(
        val number: String = "",
        val error: String? = null,
    ) : AuthStep

    data object CheckingNumber : AuthStep

    data class NumberRejected(
        val number: String,
    ) : AuthStep // 1.2b

    data class SendingCode(
        val maskedNumber: String,
    ) : AuthStep // 1.3a

    data class SendFailed(
        val number: String,
    ) : AuthStep // 1.3b

    data class Otp(
        val maskedNumber: String,
        val code: String = "",
        val status: OtpStatus = OtpStatus.Idle,
        val errorMessage: String? = null, // 1.4c
        val secondsLeft: Int = 30,
        val resendsLeft: Int = 3,
    ) : AuthStep

    data object Locked : AuthStep // 1.4d

    data class PickUnit(
        val units: List<ResidentUnit>,
        val selectedId: String? = null,
    ) : AuthStep

    data class Review(
        val details: Details,
    ) : AuthStep // 1.6
}
