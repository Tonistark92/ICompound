package com.icompound.phone_auth.domain

import com.icompound.core.domain.model.BaseDomain
import com.icompound.phone_auth.presentation.compoenents.OtpUiStatus


sealed interface AuthStep {
    data class Phone(val number: String = "", val error: String? = null) : AuthStep
    data object CheckingNumber : AuthStep
    data class NumberRejected(val number: String) : AuthStep       // 1.2b
    data class SendingCode(val maskedNumber: String) : AuthStep    // 1.3a
    data class SendFailed(val number: String) : AuthStep           // 1.3b
    data class Otp(
        val maskedNumber: String,
        val code: String = "",
        val status: OtpStatus = OtpStatus.Idle,
        val errorMessage: String? = null,   // 1.4c
        val secondsLeft: Int = 30,
        val resendsLeft: Int = 3,
    ) : AuthStep
    data object Locked : AuthStep                                  // 1.4d
    data class PickUnit(val units: List<ResidentUnit>, val selectedId: String? = null) : AuthStep
    data class Review(val details: Details) : AuthStep         // 1.6
}

enum class OtpStatus {
    Idle,       // typing allowed
    Verifying,  // 6th digit entered, waiting for server (input locked)
    Error,      // wrong code: shake, then the ViewModel resets to Idle
    Success     // correct code: wave, then the ViewModel navigates on
}
// phone_auth/presentation/state/OtpStatusMapper.kt
fun OtpStatus.toUi(): OtpUiStatus = when (this) {
    OtpStatus.Idle, OtpStatus.Verifying -> OtpUiStatus.Idle
    OtpStatus.Error -> OtpUiStatus.Error
    OtpStatus.Success -> OtpUiStatus.Success
}

/** A unit the resident can pick on screen 1.5 */
data class ResidentUnit(
    val id: String,
    val title: String,        // "Villa 14"
    val subtitle: String? = null,
)

/** Data shown on the "Review your details" screen (1.6) */
data class Details(
    val fullName: String,
    val maskedMobile: String,           // "+20 • • • • 4567"
    val unit: ResidentUnit,
    val allowNotifications: Boolean = true,
)

/** Returned by the "compound manager" endpoint */
data class ManagerContact(
    val name: String? = null,
    val phone: String,                  // raw dialable number, e.g. "+20100..."
    val email: String,                  // raw dialable number, e.g. "+20100..."
) : BaseDomain()