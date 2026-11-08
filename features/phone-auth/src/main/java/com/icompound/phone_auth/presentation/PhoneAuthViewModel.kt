package com.icompound.phone_auth.presentation

import com.icompound.core.data.model.Resource
import com.icompound.core.domain.model.exception.AppException
import com.icompound.core.presentation.base.DispatcherProvider
import com.icompound.core.presentation.base.mvi.MVIViewModel
import com.icompound.phone_auth.domain.AuthStep
import com.icompound.phone_auth.domain.OtpStatus
import com.icompound.phone_auth.domain.usecase.GetManagerContactUC
import com.icompound.phone_auth.presentation.state.ManagerContactState
import com.icompound.phone_auth.presentation.state.PhoneAuthAction
import com.icompound.phone_auth.presentation.state.PhoneAuthEvent
import com.icompound.phone_auth.presentation.state.PhoneAuthState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class PhoneAuthViewModel (
    private val getManagerContact: GetManagerContactUC,
//    private val checkNumber: CheckNumberUseCase,     // String -> NumberCheck
//    private val sendCode: SendCodeUseCase,           // String -> Unit
//    private val verifyOTPCode: VerifyOTPCodeUseCase,       // String -> VerifyResult
    dispatcherProvider: DispatcherProvider,
) : MVIViewModel<PhoneAuthAction, PhoneAuthEvent, PhoneAuthState>(
    initialState = PhoneAuthState(),
    dispatcherProvider = dispatcherProvider,
) {
    private var flowJob: Job? = null      // check / send / verify in flight
    private var timerJob: Job? = null
    private var managerJob: Job? = null

     override fun onActionTrigger(action: PhoneAuthAction) = when (action) {
        is PhoneAuthAction.PhoneChanged -> {
            state.value.number = action.number
            updateState { copy(step = AuthStep.Phone(action.number)) }
        }
        PhoneAuthAction.SendCodeClicked, PhoneAuthAction.RetrySend -> onSendCode()
        PhoneAuthAction.TryDifferentNumber, PhoneAuthAction.BackClicked -> onBack()
        is PhoneAuthAction.CodeChanged -> updateOtp { copy(code = action.code) }
        is PhoneAuthAction.CodeComplete -> onVerify(action.code)
        PhoneAuthAction.ResendClicked -> onResend()
        PhoneAuthAction.ContactManagerClicked -> onContactManager()
    }

    // ---------- manager number (lazy, retryable) ----------

    private fun loadManager() {
//        if (managerJob?.isActive == true) return
//        updateState { copy(manager = ManagerContactState.Loading) }
//        managerJob = getManagerContact(Unit).launchResult(
//            onFailure = { updateState { copy(manager = ManagerContactState.Failed) } },
//        ) { contact ->
//            updateState { copy(manager = ManagerContactState.Loaded(contact)) }
//        }
    }

    private fun onContactManager() {
        when (val m = currentViewState.manager) {
            is ManagerContactState.Loaded -> sendEvent(PhoneAuthEvent.Dial(m.contact.phone))
            ManagerContactState.Failed -> loadManager()   // retry
            ManagerContactState.Loading -> Unit
        }
    }

    // ---------- phone -> check -> send ----------

    private fun onSendCode() {
//        flowJob?.cancel()
//        setStep(AuthStep.CheckingNumber)
//        flowJob = checkNumber(number).launchResult(
//            onFailure = { setStep(AuthStep.SendFailed(number)) },
//        ) { result ->
//            when (result) {
//                NumberCheck.Registered -> sendCodeToNumber()
//                NumberCheck.NotRegistered -> {
//                    setStep(AuthStep.NumberRejected(number))
//                    loadManager()
//                }
//                is NumberCheck.Invalid -> setStep(AuthStep.Phone(number, result.message))
//            }
//        }
    }

    private fun sendCodeToNumber() {
//        setStep(AuthStep.SendingCode(mask(number)))
//        flowJob = sendCode(number).launchResult(
//            onFailure = {
//                setStep(AuthStep.SendFailed(number))
//                loadManager()
//            },
//        ) {
//            setStep(AuthStep.Otp(maskedNumber = mask(number)))
//            startTimer()
//        }
    }

    // ---------- otp ----------

    private fun onVerify(code: String) {
//        flowJob?.cancel()
//        updateOtp { copy(status = OtpStatus.Verifying, errorMessage = null) }
//        flowJob = verifyCode(code).launchResult(
//            onFailure = { resetOtp("Something went wrong. Try again.") },
//        ) { result ->
//            when (result) {
//                is VerifyResult.Valid -> viewModelScopeWithHandler.launch {
//                    updateOtp { copy(status = OtpStatus.Success) }
//                    delay(700)                       // let the wave finish
//                    setStep(AuthStep.PickUnit(result.units))
//                }
//                is VerifyResult.Wrong -> viewModelScopeWithHandler.launch {
//                    updateOtp {
//                        copy(status = OtpStatus.Error,
//                            errorMessage = "Incorrect code, ${result.attemptsLeft} attempts left before entry is locked")
//                    }
//                    delay(500)                       // let the shake finish
//                    updateOtp { copy(code = "", status = OtpStatus.Idle) }
//                }
//                VerifyResult.Locked -> {
//                    timerJob?.cancel()
//                    setStep(AuthStep.Locked)
//                    loadManager()
//                }
//            }
//        }
    }

    private fun resetOtp(message: String) =
        updateOtp { copy(code = "", status = OtpStatus.Idle, errorMessage = message) }

    private fun onResend() {
//        val otp = currentViewState.step as? AuthStep.Otp ?: return
//        if (otp.secondsLeft > 0 || otp.resendsLeft == 0) return
//        flowJob = sendCode(number).launchResult(
//            onFailure = { setStep(AuthStep.SendFailed(number)); loadManager() },
//        ) {
//            updateOtp { copy(resendsLeft = resendsLeft - 1, code = "", errorMessage = null) }
//            startTimer()
//        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScopeWithHandler.launch {
            for (s in 30 downTo 0) {
                updateOtp { copy(secondsLeft = s) }
                delay(1000)
            }
        }
    }

    // ---------- back ----------

    private fun onBack() {
        flowJob?.cancel()          // a late response can't pull the user forward
        timerJob?.cancel()
        when (val s = currentViewState.step) {
            is AuthStep.NumberRejected, is AuthStep.SendFailed, is AuthStep.Otp,
            AuthStep.Locked -> setStep(AuthStep.Phone(state.value.number!!))
            else -> Unit
        }
    }

    // ---------- helpers ----------

    private fun setStep(step: AuthStep) = updateState { copy(step = step) }

    private fun updateOtp(t: AuthStep.Otp.() -> AuthStep.Otp) = updateState {
        val s = step
        if (s is AuthStep.Otp) copy(step = s.t()) else this
    }

    private fun <T> Flow<Resource<T>>.launchResult(
        onFailure: (AppException) -> Unit = {},
        onSuccess: (T) -> Unit,
    ): Job = viewModelScopeWithHandler.launch {
        collect {
            when (it) {
                is Resource.Success -> onSuccess(it.model)
                is Resource.Failure -> onFailure(it.exception)
                is Resource.Progress<*> -> Unit   // loading is shown by AuthStep, not by this flag
            }
        }
    }
}