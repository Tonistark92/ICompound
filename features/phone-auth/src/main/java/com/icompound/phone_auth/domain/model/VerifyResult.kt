package com.icompound.phone_auth.domain.model

sealed interface VerifyResult {

    data class alid(
        val userId: String,
//        val units: List<CompoundUnit>,
    ) : VerifyResult

    data class Wrong(
        val attemptsLeft: Int,
    ) : VerifyResult

    data object Locked : VerifyResult
}