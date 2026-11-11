package com.icompound.phoneauth.presentation.state

import com.icompound.core.presentation.base.mvi.ViewAction
import com.icompound.core.presentation.base.mvi.ViewEvent
import com.icompound.core.presentation.base.mvi.ViewState
import com.icompound.phoneauth.domain.model.AuthStep
import com.icompound.phoneauth.domain.model.ManagerContact
import com.icompound.phoneauth.domain.model.ResidentUnit

sealed interface ManagerContactState {
    data object Loading : ManagerContactState

    data class Loaded(
        val contact: ManagerContact,
    ) : ManagerContactState

    data object Failed : ManagerContactState
}

data class PhoneAuthState(
    val step: AuthStep = AuthStep.Phone(),
    var number: String? = null,
    var fullName: String? = null,
    var units: List<ResidentUnit> = emptyList(),
    val manager: ManagerContactState = ManagerContactState.Loading,
) : ViewState

sealed interface PhoneAuthAction : ViewAction {
    data class PhoneChanged(
        val number: String,
    ) : PhoneAuthAction

    data object SendCodeClicked : PhoneAuthAction

    data object TryDifferentNumber : PhoneAuthAction

    data object RetrySend : PhoneAuthAction

    data object ContinueClicked : PhoneAuthAction

    data class CodeChanged(
        val code: String,
    ) : PhoneAuthAction

    data class CodeComplete(
        val code: String,
    ) : PhoneAuthAction

    data class UnitSelected(
        val id: String,
    ) : PhoneAuthAction

    data object ResendClicked : PhoneAuthAction

    data object ContactManagerClicked : PhoneAuthAction

    data object ContactSupportClicked : PhoneAuthAction

    data object FinishClicked : PhoneAuthAction

    data class NotificationsToggled(
        val flag: Boolean,
    ) : PhoneAuthAction

    data object BackClicked : PhoneAuthAction
}

sealed interface PhoneAuthEvent : ViewEvent {
    data class Dial(
        val phone: String,
    ) : PhoneAuthEvent

    data object Done : PhoneAuthEvent
}
