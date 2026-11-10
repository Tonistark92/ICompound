package com.icompound.phone_auth.domain.model

import com.icompound.phone_auth.presentation.compoenents.OtpUiStatus


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
