# Adding a feature (or a new use case)

This is the recipe to follow every time. Example A (`GetManagerContact`, a pre-login call with no inputs) is built end to end. Example B (maintenance tickets, a post-login call that needs the user id) shows only what differs.

> Endpoint paths and JSON field names in the examples are **placeholders**. Always take the real ones from the EyeCompound OpenAPI spec / Postman collection.

## 0. Decide before you write code

1. **Name the feature** in `snake_case` (`phone_auth`, `maintenance`). New business area → new feature package. New call in an existing area → new use case in that feature.
2. **List the use cases** as `<Verb><Noun>UC`.
3. **Pick the base class** for each use case:

| Situation | Extend | Body type |
|---|---|---|
| Pre-login call, no inputs | `BaseUseCase<Domain, Unit>` | `Unit`, call it as `uc(Unit)` |
| Pre-login call with inputs | `BaseUseCase<Domain, XxxRequest>` | request class (add the `X-No-Auth` header) |
| Call that returns the token / user | `BaseUseCase` + `ISessionRepository` | request class |
| Any call after login | `AuthenticatedUseCase<Domain, Body>` | params or request class |

4. **Where do the endpoint's inputs go?** Body, query and header values go into `RemoteRequest`. **Path parameters** (such as the user id in `users/{id}/bills`) are function arguments of the repository, not part of `RemoteRequest`.

## 1. Create the skeleton

```
com.icompound.<feature>/
├── data/{dto,mapper,repository}
├── domain/{interactor,model,model/request,repository}
├── presentation/
└── di/
```

(A brand-new feature that is its own Gradle module: see [adding-a-module.md](docs/adding-a-module.md).)

## 2. DTO: `data/dto/`

The JSON shape of the response. Extend `BaseDto` (it carries `message` and `code`). **Every field is nullable with a default**: Gson ignores Kotlin null-safety.

```kotlin
package com.icompound.phone_auth.data.dto

data class ManagerContactDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
)

/** Envelope { "data": {...}, "message": "...", "code": "..." }. If the response is flat, make ManagerContactDto extend BaseDto instead. */
data class ManagerContactResponseDto(
    @SerializedName("data") val data: ManagerContactDto? = null,
) : BaseDto()
```

Enums arrive as `String?` and are converted in the mapper.

## 3. Domain model: `domain/model/`

What the rest of the app sees. Non-null fields, helpers allowed. Extend `BaseDomain` when the API returns `message` / `code`.

```kotlin
package com.icompound.phone_auth.domain.model

data class ManagerContact(
    val name: String,
    val phone: String,
    val email: String,
) : BaseDomain() {
    val hasPhone: Boolean get() = phone.isNotBlank()
}
```

## 4. Mapper: `data/mapper/`

`internal`, extends `Mapper<Dto, Domain, Entity>`. Use `Unit` as the entity type when nothing is stored locally. Override only `dtoToDomain`. All null handling lives here.

```kotlin
package com.icompound.phone_auth.data.mapper

internal class ManagerContactMapper : Mapper<ManagerContactResponseDto, ManagerContact, Unit>() {
    override fun dtoToDomain(model: ManagerContactResponseDto): ManagerContact =
        ManagerContact(
            name = model.data?.name.orEmpty().trim(),
            phone = model.data?.phone.orEmpty().trim(),
            email = model.data?.email.orEmpty().trim(),
        ).also { model.mapBaseFields(it) }      // copies message and code
}
```

## 5. Request: `domain/model/request/`

Only when the call has inputs. It implements `IRequestValidation`: it builds the `RemoteRequest` and declares the **contract** (`true` = required, `false` = optional).

```kotlin
package com.icompound.phone_auth.domain.model.request

class CheckNumberRequest(private val phone: String) : IRequestValidation {

    override val remoteMap = RemoteRequest(
        requestBody = hashMapOf(PHONE to phone),
        requestHeaders = hashMapOf(TokenAuthenticator.NO_AUTH_HEADER to "true"),   // pre-login: no bearer token
    )

    override fun isInitialState() = phone.isBlank()

    override fun getRequestContracts() = hashMapOf(
        RequestContractType.BODY to hashMapOf(PHONE to true),
        RequestContractType.HEADERS to hashMapOf(TokenAuthenticator.NO_AUTH_HEADER to true),
    )

    private companion object { const val PHONE = "phone" }
}
```

Rules: declare **every** key you put in `remoteMap` (extra keys fail the size check); keys must be unique across body, queries and headers (they are merged for validation). No inputs at all? Skip this step and use `Unit`.

## 6. Repository interface: `domain/repository/`

One function per endpoint. Takes a `RemoteRequest` (or nothing), returns a domain model, no Android or Retrofit types.

```kotlin
package com.icompound.phone_auth.domain.repository

interface IPhoneAuthRepository {
    suspend fun getManagerContact(): ManagerContact
    suspend fun checkNumber(request: RemoteRequest): NumberCheck
}
```

## 7. Repository implementation: `data/repository/`

`internal`. Send the request through `INetworkProvider`, map the DTO. **No `try/catch`, no validation** (the use case does both).

```kotlin
package com.icompound.phone_auth.data.repository

internal class PhoneAuthRepository(
    private val network: INetworkProvider,
    private val managerContactMapper: ManagerContactMapper,
    private val numberCheckMapper: NumberCheckMapper,
) : IPhoneAuthRepository {

    override suspend fun getManagerContact(): ManagerContact {
        val dto = network.get<ManagerContactResponseDto>(
            responseWrappedModel = ManagerContactResponseDto::class.java,
            pathUrl = MANAGER_CONTACT_PATH,
        )
        return managerContactMapper.dtoToDomain(dto)
    }

    override suspend fun checkNumber(request: RemoteRequest): NumberCheck {
        val dto = network.post<NumberCheckResponseDto, Map<String, Any>>(
            responseWrappedModel = NumberCheckResponseDto::class.java,
            pathUrl = CHECK_NUMBER_PATH,
            headers = request.requestHeaders,
            queryParams = request.requestQueries,
            requestBody = request.requestBody,
        )
        return numberCheckMapper.dtoToDomain(dto)
    }

    private companion object {
        const val MANAGER_CONTACT_PATH = "manager/contact"    // placeholder: no leading slash
        const val CHECK_NUMBER_PATH = "auth/check-number"     // placeholder
    }
}
```

`pathUrl` has no leading slash; the base URL ends with `/`. For a generic response use `object : TypeToken<ApiList<Dto>>() {}.type`.

## 8. Use case: `domain/interactor/`

The error boundary. Validate, call the repository, emit.

```kotlin
// A. pre-login, no inputs
class GetManagerContactUC(
    private val repo: IPhoneAuthRepository,
    errorHandler: ErrorHandler,
    dispatchers: DispatcherProvider,
) : BaseUseCase<ManagerContact, Unit>(errorHandler, dispatchers) {

    override fun executeDS(body: Unit?): Flow<ManagerContact> = flow {
        emit(repo.getManagerContact())
    }
}

// B. with a request
class CheckNumberUC(
    private val repo: IPhoneAuthRepository,
    errorHandler: ErrorHandler,
    dispatchers: DispatcherProvider,
) : BaseUseCase<NumberCheck, CheckNumberRequest>(errorHandler, dispatchers) {

    override fun executeDS(body: CheckNumberRequest?): Flow<NumberCheck> = flow {
        val request = requireBody(body)
        request.validateRequestContract()
        emit(repo.checkNumber(request.remoteMap))
    }
}
```

Never `try/catch` here: throw, and `BaseUseCase` converts everything to `Resource.Failure`. The use case that returns the token also saves the session (see [core-components.md](../../../Downloads/core-components.md#14-session-and-authenticatedusecase)).

## 9. Koin module: `di/`

```kotlin
package com.icompound.phone_auth.di

val phoneAuthModule = module {
    factory { ManagerContactMapper() }
    factory { NumberCheckMapper() }
    single<IPhoneAuthRepository> { PhoneAuthRepository(get(), get(), get()) }
    factoryOf(::GetManagerContactUC)
    factoryOf(::CheckNumberUC)
    viewModelOf(::PhoneAuthViewModel)
}
```

| Type | Koin |
|---|---|
| Mapper | `factory` |
| Repository | `single` |
| Use case | `factoryOf(::XxxUC)` |
| ViewModel | `viewModelOf(::XxxViewModel)` (**never** `single`) |

Then add `phoneAuthModule` to `modules(...)` in **every app that uses the feature**. Forgetting this is the number one cause of `NoDefinitionFoundException`.

## 10. Contract: `presentation/<Screen>Contract.kt`

```kotlin
internal sealed interface PhoneAuthAction : ViewAction {
    data class PhoneChanged(val number: String) : PhoneAuthAction
    data object SendCodeClicked : PhoneAuthAction
    data object ContactManagerClicked : PhoneAuthAction
}

internal sealed interface PhoneAuthEvent : ViewEvent {
    data class Dial(val phone: String) : PhoneAuthEvent
    data object NavigateToHome : PhoneAuthEvent
}

internal data class PhoneAuthState(
    val number: String = "",
    val isLoading: Boolean = false,
    val manager: ManagerContactState = ManagerContactState.Loading,
    val error: AppException? = null,
) : ViewState
```

Action = verbs the user does. Event = effects that must happen **once**. State = everything drawn, with defaults, so `State()` is a valid initial state. Derive values from state; do not keep a second copy in a `private var`.

## 11. ViewModel: `presentation/<Screen>ViewModel.kt`

Extends `MVIViewModel`. Use the Flow-style use case with `launchResult` and keep the `Job` when the work must be cancellable.

```kotlin
internal class PhoneAuthViewModel(
    private val getManagerContact: GetManagerContactUC,
    private val checkNumber: CheckNumberUC,
    dispatcherProvider: DispatcherProvider,
) : MVIViewModel<PhoneAuthAction, PhoneAuthEvent, PhoneAuthState>(
    initialState = PhoneAuthState(),
    dispatcherProvider = dispatcherProvider,
) {
    private var managerJob: Job? = null
    private var flowJob: Job? = null

    override fun onActionTrigger(action: PhoneAuthAction) {
        when (action) {
            is PhoneAuthAction.PhoneChanged -> updateState { copy(number = action.number) }
            PhoneAuthAction.SendCodeClicked -> onSendCode()
            PhoneAuthAction.ContactManagerClicked -> onContactManager()
        }
    }

    private fun loadManager() {
        if (managerJob?.isActive == true) return
        updateState { copy(manager = ManagerContactState.Loading) }
        managerJob = getManagerContact(Unit).launchResult(
            onFailure = { updateState { copy(manager = ManagerContactState.Failed) } },
        ) { contact -> updateState { copy(manager = ManagerContactState.Loaded(contact)) } }
    }

    private fun onSendCode() {
        flowJob?.cancel()
        updateState { copy(error = null) }
        flowJob = checkNumber(CheckNumberRequest(currentViewState.number)).launchResult(
            onProgress = { loading -> updateState { copy(isLoading = loading) } },
            onFailure = { e -> updateState { copy(error = e) } },
        ) { /* success: continue the flow */ }
    }
    // onContactManager(): Loaded → sendEvent(Dial(phone)), Failed → loadManager(), Loading → ignore
}
```

Rules: no `try/catch`; `Resource.Failure` becomes `error` in state (or an event for one-shot messages); never touch DTOs.

## 12. Screen and navigation: `presentation/<Screen>Screen.kt`

```kotlin
@Composable
fun PhoneAuthRoute(onNavigateHome: () -> Unit, viewModel: PhoneAuthViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.singleEvent.collect { event ->
            when (event) {
                is PhoneAuthEvent.Dial -> context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${event.phone}")))
                PhoneAuthEvent.NavigateToHome -> onNavigateHome()
            }
        }
    }
    PhoneAuthScreen(state = state, onAction = viewModel::processIntent)
}

@Composable
private fun PhoneAuthScreen(state: PhoneAuthState, onAction: (PhoneAuthAction) -> Unit) { /* stateless UI, previewable */ }
```

Show errors with the shared `AppException.asMessage()` (never `exception.message`). Register `PhoneAuthRoute` in each app's navigation graph; the feature never navigates to another feature directly (the app passes callbacks).

## 13. Previews and screenshots

Every screen ships with `@Preview`s for **all states × light/dark × every supported language (including RTL) × large font**. Because the screen composable is stateless, a `PreviewParameterProvider` can feed it every state:

```kotlin
class PhoneAuthStateProvider : PreviewParameterProvider<PhoneAuthState> {
    override val values = sequenceOf(
        PhoneAuthState(),                                                                     // initial
        PhoneAuthState(number = "01000000000"),                                               // filled
        PhoneAuthState(isLoading = true),                                                     // loading
        PhoneAuthState(error = AppException.Local.IOOperation(R.string.error_io_unexpected_message)),   // offline
        PhoneAuthState(manager = ManagerContactState.Failed),                                 // manager failed
    )
}

@ThemeLanguagePreviews                       // shared annotation: light/dark × EN/AR × font scale
@Composable
private fun PhoneAuthScreenPreview(@PreviewParameter(PhoneAuthStateProvider::class) state: PhoneAuthState) {
    AppTheme { PhoneAuthScreen(state = state, onAction = {}) }
}
```

Then take screenshots of each state/theme/language (from the preview panel or an emulator) and put them in the PR table. The annotation, the list of cases to cover and tips: [docs/testing-and-quality.md](testing-and-quality.md#2-compose-previews-and-screenshots).

## 14. Tests

Write them with the feature, not after:

* **Unit:** mapper, request contract, use case, repository (MockWebServer), ViewModel. Templates: [docs/testing-and-quality.md](testing-and-quality.md#1-unit-tests).
* **Instrumented:** required when the feature is a flow across screens or features, or depends on Android behaviour. Add `Modifier.testTag("<screen>_<element>")` to the nodes the test needs. Rules and a skeleton: [docs/testing-and-quality.md](testing-and-quality.md#3-instrumented-tests).

## 15. Finish: the Definition of Done and the PR

1. Run the hooks' checks (`git push` triggers them): Spotless, Detekt, Konsist, unit tests, lint.
2. Build a minified variant and exercise the feature (R8 rules in `consumer-rules.pro`).
3. Commit with the [commit standard](contributing.md#4-commit-standard); the PR title uses it too.
4. Fill in the PR template: **screenshots**, previews, tests, checklist.
5. Check the [Definition of Done](../README.md#definition-of-done-every-feature).

---

## Example B: a post-login use case (maintenance tickets)

Differences from example A:

```kotlin
// request: optional filter + page (queries)
class GetTicketsRequest(page: Int = 1, status: String? = null) : IRequestValidation {
    override val remoteMap = RemoteRequest(
        requestQueries = hashMapOf<String, Any>(PAGE to page).apply { status?.let { put(STATUS, it) } },
    )
    override fun isInitialState() = false
    override fun getRequestContracts() = hashMapOf(
        RequestContractType.QUERIES to hashMapOf(PAGE to true, STATUS to false),   // status is optional
    )
    private companion object { const val PAGE = "page"; const val STATUS = "status" }
}

// repository: the user id is a PATH parameter, so it is a function argument
interface IMaintenanceRepository {
    suspend fun getTickets(userId: String, request: RemoteRequest): MaintenanceTickets
}
// implementation: network.get<MaintenanceTicketsDto>(…, pathUrl = "users/$userId/tickets", headers = request.requestHeaders, queryParams = request.requestQueries)

// use case: extends AuthenticatedUseCase, so userId() is one line; the token is added by HeaderInterceptor
class GetTicketsUC(
    private val repo: IMaintenanceRepository,
    session: ISessionRepository,
    errorHandler: ErrorHandler,
    dispatchers: DispatcherProvider,
) : AuthenticatedUseCase<MaintenanceTickets, GetTicketsRequest>(session, errorHandler, dispatchers) {

    override fun executeDS(body: GetTicketsRequest?): Flow<MaintenanceTickets> = flow {
        val request = requireBody(body)
        request.validateRequestContract()
        emit(repo.getTickets(userId(), request.remoteMap))
    }
}
```

If the user is not logged in, `userId()` throws `Client.Unauthorized`, which arrives as `Resource.Failure` like any other error.

## Adding one more call to an existing feature

DTO (if the shape is new) → domain model → mapper → request → function in `I<Feature>Repository` and `<Feature>Repository` → use case → `factoryOf` in the feature module → ViewModel branch → tests. Nothing in `core` changes.

## Common mistakes

| Symptom | Cause |
|---|---|
| `NoDefinitionFoundException` | the feature's Koin module is missing from the app's `modules(...)`, or a `factoryOf` is missing |
| `RequestValidation` failure on a valid request | a key in `remoteMap` is not declared in the contract, or the same key is in two of body / queries / headers |
| Old screen state reappears | the ViewModel was registered as `single` |
| Crash only in release | a DTO or domain model needs R8 keep rules (`consumer-rules.pro`) |
| Empty or null parsing | DTO field names differ from the JSON; DTO fields not nullable |
| 401 on the login call triggers a refresh | the login request is missing the `X-No-Auth` header |
| Token missing on a protected call | login did not call `session.saveSession(user)` before emitting |
