package com.icompound.phone_auth.presentation

import com.icompound.core.presentation.base.DispatcherProvider
import com.icompound.core.presentation.base.mvi.MVIViewModel
import com.icompound.phone_auth.domain.AuthStep
import com.icompound.phone_auth.domain.Details
import com.icompound.phone_auth.domain.ManagerContact
import com.icompound.phone_auth.domain.OtpStatus
import com.icompound.phone_auth.domain.ResidentUnit
import com.icompound.phone_auth.presentation.state.ManagerContactState
import com.icompound.phone_auth.presentation.state.PhoneAuthAction
import com.icompound.phone_auth.presentation.state.PhoneAuthEvent
import com.icompound.phone_auth.presentation.state.PhoneAuthState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** MOCK_FLOW: behaves like the real flow with fake results. NAVIGATOR: every button goes to the next screen. */
private enum class DemoMode { MOCK_FLOW, NAVIGATOR }

private val DEMO_MODE = DemoMode.MOCK_FLOW   // <- change this one line to switch

class PhoneAuthViewModel(
    // private val getManagerContact: GetManagerContactUC,
    // private val checkNumber: CheckNumberUseCase,
    // private val sendCode: SendCodeUseCase,
    // private val verifyOTPCode: VerifyOTPCodeUseCase,
    dispatcherProvider: DispatcherProvider,
) : MVIViewModel<PhoneAuthAction, PhoneAuthEvent, PhoneAuthState>(
    initialState = PhoneAuthState(),
    dispatcherProvider = dispatcherProvider,
) {
    private var number = ""
    private var fullName = ""
    private var units: List<ResidentUnit> = emptyList()

    private var flowJob: Job? = null
    private var timerJob: Job? = null

    private val demoManager = ManagerContactState.Loaded(
        ManagerContact(phone = "+201000000000", email = "manager@icompound.com")
    )

    init {
        // Contact buttons are always enabled in the demo
        updateState { copy(manager = demoManager) }
    }

    override fun onActionTrigger(action: PhoneAuthAction) {
        when (DEMO_MODE) {
            DemoMode.MOCK_FLOW -> mockFlow(action)
            DemoMode.NAVIGATOR -> navigator(action)
        }
    }

    // =====================================================================
    // MOCK_FLOW
    //  number ends with 0 -> number rejected      number ends with 1 -> inline error
    //  number ends with 2 -> couldn't send        anything else      -> OTP
    //  code 000000 -> locked                      code 111111        -> success
    //  any other code -> wrong, attempts decrease (locked at 0)
    // =====================================================================

    private var demoAttempts = 3
    private val demoUnits = listOf(
        ResidentUnit("1", "Villa 14", "Phase 2"),
        ResidentUnit("2", "Apartment 8", "Building C"),
    )

    private fun mockFlow(action: PhoneAuthAction) {
        when (action) {
            is PhoneAuthAction.PhoneChanged -> {
                number = action.number
                if (currentViewState.step is AuthStep.Phone) setStep(AuthStep.Phone(action.number))
            }
            PhoneAuthAction.SendCodeClicked, PhoneAuthAction.RetrySend -> mockSendCode()
            PhoneAuthAction.TryDifferentNumber, PhoneAuthAction.BackClicked -> mockBack()
            is PhoneAuthAction.CodeChanged -> updateOtp { copy(code = action.code) }
            is PhoneAuthAction.CodeComplete -> mockVerify(action.code)
            PhoneAuthAction.ResendClicked -> {
                updateOtp {
                    copy(resendsLeft = (resendsLeft - 1).coerceAtLeast(0), code = "", errorMessage = null)
                }
                startTimer()
            }
            PhoneAuthAction.ContactManagerClicked,
            PhoneAuthAction.ContactSupportClicked -> sendEvent(PhoneAuthEvent.Dial("+201000000000"))

            is PhoneAuthAction.UnitSelected -> updatePick { copy(selectedId = action.id) }
            PhoneAuthAction.ContinueClicked -> {
                val s = currentViewState.step as? AuthStep.PickUnit ?: return
                val unit = s.units.firstOrNull { it.id == s.selectedId } ?: return
                setStep(AuthStep.Review(Details(fullName, mask(number), unit)))
            }
            is PhoneAuthAction.NotificationsToggled -> updateReview {
                copy(details = details.copy(allowNotifications = action.flag))
            }
            PhoneAuthAction.FinishClicked -> sendEvent(PhoneAuthEvent.Done)
        }
    }

    private fun mockSendCode() {
        flowJob?.cancel()
        flowJob = viewModelScopeWithHandler.launch {
            setStep(AuthStep.CheckingNumber)
            delay(1200)
            when {
                number.endsWith("0") -> { setStep(AuthStep.NumberRejected(number)); return@launch }
                number.endsWith("1") -> {
                    setStep(AuthStep.Phone(number, "That number doesn't look right. Enter 9 digits starting with 1."))
                    return@launch
                }
            }
            setStep(AuthStep.SendingCode(mask(number)))
            delay(1200)
            if (number.endsWith("2")) { setStep(AuthStep.SendFailed(number)); return@launch }
            demoAttempts = 3
            setStep(AuthStep.Otp(maskedNumber = mask(number)))
            startTimer()
        }
    }

    private fun mockVerify(code: String) {
        val otp = currentViewState.step as? AuthStep.Otp ?: return
        if (otp.status != OtpStatus.Idle) return
        flowJob?.cancel()
        flowJob = viewModelScopeWithHandler.launch {
            updateOtp { copy(status = OtpStatus.Verifying, errorMessage = null) }
            delay(1000)
            when {
                code == "000000" -> { timerJob?.cancel(); setStep(AuthStep.Locked) }
                code == "111111" -> {
                    fullName = "Islam Hany"
                    units = demoUnits
                    updateOtp { copy(status = OtpStatus.Success) }
                    delay(700)
                    setStep(AuthStep.PickUnit(units))
                }
                else -> {
                    demoAttempts--
                    if (demoAttempts <= 0) { timerJob?.cancel(); setStep(AuthStep.Locked); return@launch }
                    updateOtp {
                        copy(
                            status = OtpStatus.Error,
                            errorMessage = "Incorrect code, $demoAttempts attempts left before entry is locked",
                        )
                    }
                    delay(500)
                    updateOtp { copy(code = "", status = OtpStatus.Idle) }
                }
            }
        }
    }

    private fun mockBack() {
        flowJob?.cancel()
        timerJob?.cancel()
        when (val s = currentViewState.step) {
            is AuthStep.NumberRejected, is AuthStep.SendFailed, is AuthStep.Otp,
            AuthStep.Locked -> setStep(AuthStep.Phone(number))
            is AuthStep.Review ->
                if (units.size > 1) setStep(AuthStep.PickUnit(units, s.details.unit.id))
            else -> Unit
        }
    }

    // =====================================================================
    // NAVIGATOR: every button goes to the next screen, Back goes to the previous one
    // =====================================================================

    private val demoSteps: List<AuthStep> by lazy {
        val unit = ResidentUnit("1", "Villa 14", "Phase 2")
        val masked = "+20 • • • • 4567"
        listOf(
            AuthStep.Phone(),                                                         // 1.1a
            AuthStep.Phone("1011945698", "That number doesn't look right."),          // 1.1b
            AuthStep.CheckingNumber,                                                  // 1.2a
            AuthStep.NumberRejected("1011945698"),                                    // 1.2b
            AuthStep.SendingCode(masked),                                             // 1.3a
            AuthStep.SendFailed("1011945698"),                                        // 1.3b
            AuthStep.Otp(masked, code = "47"),                                        // 1.4a
            AuthStep.Otp(
                masked, code = "472021", status = OtpStatus.Error,
                errorMessage = "Incorrect code, 2 attempts left before entry is locked",
                secondsLeft = 0, resendsLeft = 3,
            ),                                                                        // 1.4c
            AuthStep.Locked,                                                          // 1.4d
            AuthStep.PickUnit(listOf(unit, ResidentUnit("2", "Apartment 8", "Building C"))), // 1.5
            AuthStep.Review(Details("Islam Hany", masked, unit)),                     // 1.6
        )
    }
    private var demoIndex = 0

    private fun demoGo(index: Int) {
        demoIndex = index.coerceIn(0, demoSteps.lastIndex)
        setStep(demoSteps[demoIndex])
    }

    private fun navigator(action: PhoneAuthAction) {
        when (action) {
            PhoneAuthAction.BackClicked -> demoGo(demoIndex - 1)

            // Keep the form screens usable
            is PhoneAuthAction.PhoneChanged -> number = action.number
            is PhoneAuthAction.CodeChanged -> updateOtp { copy(code = action.code) }
            is PhoneAuthAction.UnitSelected -> updatePick { copy(selectedId = action.id) }
            is PhoneAuthAction.NotificationsToggled -> updateReview {
                copy(details = details.copy(allowNotifications = action.flag))
            }
            PhoneAuthAction.FinishClicked -> sendEvent(PhoneAuthEvent.Done)

            // Everything else (Send Code, Retry, Verify, Continue, Contact...) goes to the next screen
            else -> demoGo(demoIndex + 1)
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScopeWithHandler.launch {
            for (s in 30 downTo 0) {
                updateOtp { copy(secondsLeft = s) }
                delay(1000)
            }
        }
    }

    private fun mask(number: String): String =
        "+20 • • • • ${number.filter(Char::isDigit).takeLast(4)}"

    private fun setStep(step: AuthStep) = updateState { copy(step = step) }

    private fun updateOtp(t: AuthStep.Otp.() -> AuthStep.Otp) = updateState {
        val s = step
        if (s is AuthStep.Otp) copy(step = s.t()) else this
    }

    private fun updatePick(t: AuthStep.PickUnit.() -> AuthStep.PickUnit) = updateState {
        val s = step
        if (s is AuthStep.PickUnit) copy(step = s.t()) else this
    }

    private fun updateReview(t: AuthStep.Review.() -> AuthStep.Review) = updateState {
        val s = step
        if (s is AuthStep.Review) copy(step = s.t()) else this
    }
}
//
//import com.icompound.core.data.model.Resource
//import com.icompound.core.domain.model.exception.AppException
//import com.icompound.core.presentation.base.DispatcherProvider
//import com.icompound.core.presentation.base.mvi.MVIViewModel
//import com.icompound.phone_auth.domain.AuthStep
//import com.icompound.phone_auth.domain.AuthStep.*
//import com.icompound.phone_auth.domain.Details
//import com.icompound.phone_auth.domain.NumberCheck
//import com.icompound.phone_auth.domain.OtpStatus
//import com.icompound.phone_auth.domain.ResidentUnit
//import com.icompound.phone_auth.domain.VerifyResult
//import com.icompound.phone_auth.domain.usecase.GetManagerContactUC
//import com.icompound.phone_auth.presentation.state.ManagerContactState
//import com.icompound.phone_auth.presentation.state.PhoneAuthAction
//import com.icompound.phone_auth.presentation.state.PhoneAuthEvent
//import com.icompound.phone_auth.presentation.state.PhoneAuthState
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.launch
//
//class PhoneAuthViewModel(
//    private val getManagerContact: GetManagerContactUC,
////    private val checkNumber: CheckNumberUseCase,          // String -> NumberCheck
////    private val sendCode: SendCodeUseCase,                // String -> Unit
////    private val verifyOTPCode: VerifyOTPCodeUseCase,      // String -> VerifyResult
//    dispatcherProvider: DispatcherProvider,
//) : MVIViewModel<PhoneAuthAction, PhoneAuthEvent, PhoneAuthState>(
//    initialState = PhoneAuthState(),
//    dispatcherProvider = dispatcherProvider,
//) {
//    // Data that is not drawn directly, so it stays out of the UI state
//    private var number = ""
//    private var fullName = ""
//    private var units: List<ResidentUnit> = emptyList()
//
//    private var flowJob: Job? = null      // check / send / verify in flight
//    private var timerJob: Job? = null
//    private var managerJob: Job? = null
//
//    override fun onActionTrigger(action: PhoneAuthAction) {
//        when (action) {
//            is PhoneAuthAction.PhoneChanged -> {
//                number = action.number
//                if (currentViewState.step is AuthStep.Phone) {
//                    setStep(Phone(action.number))
//                }
//            }
//            PhoneAuthAction.SendCodeClicked, PhoneAuthAction.RetrySend -> onSendCode()
//            PhoneAuthAction.TryDifferentNumber, PhoneAuthAction.BackClicked -> onBack()
//            is PhoneAuthAction.CodeChanged -> updateOtp { copy(code = action.code) }
//            is PhoneAuthAction.CodeComplete -> onVerify(action.code)
//            PhoneAuthAction.ResendClicked -> onResend()
//            PhoneAuthAction.ContactManagerClicked,
//            PhoneAuthAction.ContactSupportClicked -> onContactManager()   // same number for now
//
//            is PhoneAuthAction.UnitSelected -> updatePick { copy(selectedId = action.id) }
//            PhoneAuthAction.ContinueClicked -> onUnitConfirmed()
////            is PhoneAuthAction.NotificationsToggled -> updateReview {
////                copy(details = details.copy(allowNotifications = action.enabled))
////            }
//            PhoneAuthAction.FinishClicked -> sendEvent(PhoneAuthEvent.Done)   // the screen navigates
//            is PhoneAuthAction.NotificationsToggled -> TODO()
//        }
//    }
//
//    // ---------- manager number (lazy, retryable) ----------
//
//    private fun loadManager() {
////        if (managerJob?.isActive == true) return
////        updateState { copy(manager = ManagerContactState.Loading) }
////        managerJob = getManagerContact(Unit).launchResult(
////            onFailure = { updateState { copy(manager = ManagerContactState.Failed) } },
////        ) { contact ->
////            updateState { copy(manager = ManagerContactState.Loaded(contact)) }
////        }
//    }
//
//    private fun onContactManager() {
//        when (val m = currentViewState.manager) {
//            is ManagerContactState.Loaded -> sendEvent(PhoneAuthEvent.Dial(m.contact.phone))
//            ManagerContactState.Failed -> loadManager()   // retry
//            ManagerContactState.Loading -> Unit
//        }
//    }
//
//    // ---------- phone -> check -> send ----------
//
//    private fun onSendCode() {
////        flowJob?.cancel()
////        setStep(AuthStep.CheckingNumber)
////        flowJob = checkNumber(number).launchResult(
////            onFailure = { setStep(AuthStep.SendFailed(number)); loadManager() },
////        ) { result ->
////            when (result) {
////                NumberCheck.Registered -> sendCodeToNumber()
////                NumberCheck.NotRegistered -> {
////                    setStep(AuthStep.NumberRejected(number))
////                    loadManager()
////                }
////                is NumberCheck.Invalid -> setStep(AuthStep.Phone(number, result.message))
////            }
////        }
//    }
//
//    private fun sendCodeToNumber() {
////        setStep(AuthStep.SendingCode(mask(number)))
////        flowJob = sendCode(number).launchResult(
////            onFailure = {
////                setStep(AuthStep.SendFailed(number))
////                loadManager()
////            },
////        ) {
////            setStep(AuthStep.Otp(maskedNumber = mask(number)))
////            startTimer()
////        }
//    }
//
//    // ---------- otp ----------
//
//    private fun onVerify(code: String) {
////        val otp = currentViewState.step as? AuthStep.Otp ?: return
////        if (otp.status != OtpStatus.Idle) return        // Verify button + onComplete can both fire
////
////        flowJob?.cancel()
////        updateOtp { copy(status = OtpStatus.Verifying, errorMessage = null) }
////        flowJob = verifyOTPCode(code).launchResult(
////            onFailure = { resetOtp("Something went wrong. Try again.") },
////        ) { result ->
////            when (result) {
////                is VerifyResult.Valid -> {
////                    fullName = result.fullName
////                    units = result.units
////                    updateOtp { copy(status = OtpStatus.Success) }
////                    delay(700)                           // let the wave finish
////                    setStep(
////                        if (units.size == 1) {
////                            AuthStep.Review(Details(fullName, mask(number), units.first()))
////                        } else {
////                            AuthStep.PickUnit(units)
////                        }
////                    )
////                }
////                is VerifyResult.Wrong -> {
////                    updateOtp {
////                        copy(
////                            status = OtpStatus.Error,
////                            errorMessage = "Incorrect code, ${result.attemptsLeft} attempts left before entry is locked",
////                        )
////                    }
////                    delay(500)                           // let the shake finish
////                    updateOtp { copy(code = "", status = OtpStatus.Idle) }
////                }
////                VerifyResult.Locked -> {
////                    timerJob?.cancel()
////                    setStep(AuthStep.Locked)
////                    loadManager()
////                }
////            }
////        }
//    }
//
//    private fun resetOtp(message: String) =
//        updateOtp { copy(code = "", status = OtpStatus.Idle, errorMessage = message) }
//
//    private fun onResend() {
////        val otp = currentViewState.step as? AuthStep.Otp ?: return
////        if (otp.secondsLeft > 0 || otp.resendsLeft == 0) return
////        flowJob?.cancel()
////        flowJob = sendCode(number).launchResult(
////            onFailure = { setStep(AuthStep.SendFailed(number)); loadManager() },
////        ) {
////            updateOtp { copy(resendsLeft = resendsLeft - 1, code = "", errorMessage = null) }
////            startTimer()
////        }
//    }
//
//    private fun startTimer() {
//        timerJob?.cancel()
//        timerJob = viewModelScopeWithHandler.launch {
//            for (s in 30 downTo 0) {
//                updateOtp { copy(secondsLeft = s) }
//                delay(1000)
//            }
//        }
//    }
//
//    // ---------- units / review ----------
//
//    private fun onUnitConfirmed() {
//        val s = currentViewState.step as? AuthStep.PickUnit ?: return
//        val unit = s.units.firstOrNull { it.id == s.selectedId } ?: return
//        setStep(AuthStep.Review(Details(fullName, mask(number), unit)))
//    }
//
//    // ---------- back ----------
//
//    private fun onBack() {
//        flowJob?.cancel()          // a late response can't pull the user forward
//        timerJob?.cancel()
//        when (val s = currentViewState.step) {
//            is AuthStep.NumberRejected, is AuthStep.SendFailed, is AuthStep.Otp,
//            AuthStep.Locked -> setStep(AuthStep.Phone(number))
//            is AuthStep.Review ->
//                if (units.size > 1) setStep(AuthStep.PickUnit(units, s.details.unit.id))
//            else -> Unit
//        }
//    }
//
//    // ---------- helpers ----------
//
//    private fun mask(number: String): String =
//        "+20 • • • • ${number.filter(Char::isDigit).takeLast(4)}"
//
//    private fun setStep(step: AuthStep) = updateState { copy(step = step) }
//
//    private fun updateOtp(t: AuthStep.Otp.() -> AuthStep.Otp) = updateState {
//        val s = step
//        if (s is AuthStep.Otp) copy(step = s.t()) else this
//    }
//
//    private fun updatePick(t: AuthStep.PickUnit.() -> AuthStep.PickUnit) = updateState {
//        val s = step
//        if (s is AuthStep.PickUnit) copy(step = s.t()) else this
//    }
//
//    private fun updateReview(t: AuthStep.Review.() -> AuthStep.Review) = updateState {
//        val s = step
//        if (s is AuthStep.Review) copy(step = s.t()) else this
//    }
//
//    private fun <T> Flow<Resource<T>>.launchResult(
//        onFailure: suspend (AppException) -> Unit = {},
//        onSuccess: suspend (T) -> Unit,                 // suspend: delays stay inside the job
//    ): Job = viewModelScopeWithHandler.launch {
//        collect {
//            when (it) {
//                is Resource.Success -> onSuccess(it.model)
//                is Resource.Failure -> onFailure(it.exception)
//                is Resource.Progress<*> -> Unit   // loading is shown by AuthStep, not by this flag
//            }
//        }
//    }
//}