package com.icompound.phoneauth.domain.model

sealed interface VerifyResult {
    data class Valid(
        val userId: String,
//        val units: List<CompoundUnit>,
    ) : VerifyResult

    data class Wrong(
        val attemptsLeft: Int,
    ) : VerifyResult

    data object Locked : VerifyResult
}
