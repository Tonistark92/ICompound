# Testing and quality

Every finished feature ships with: **unit tests, optional integration tests, Konsist architecture tests, lint, and ProGuard / R8 rules.**

## 1. Unit tests

| Class | What to test | Doubles |
|---|---|---|
| Mapper | null DTO fields become defaults; enums; trimming | none |
| Request | missing / empty / extra keys fail; optional keys pass | none |
| Use case | emission order, failure mapping, repository called with the right `remoteMap` | fake repository, fake `ErrorHandler`, `TestDispatchers` |
| Repository | path, headers, query, body, parsing, HTTP errors | `MockWebServer` |
| ViewModel | state after each action, events, cancel / retry | fake repository + real use case |

### Shared test helpers (put them in a small `core-testing` module or `testFixtures` so features do not copy them)

```kotlin
class TestDispatchers(scheduler: TestCoroutineScheduler) : DispatcherProvider {
    private val dispatcher = StandardTestDispatcher(scheduler)
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
}

/** viewModelScope uses Dispatchers.Main.immediate, so ViewModel tests must replace it. */
class MainDispatcherRule(private val dispatcher: TestDispatcher = StandardTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

val passThroughErrorHandler = object : ErrorHandler {
    override fun getError(throwable: Throwable): AppException =
        throwable as? AppException ?: AppException.Unknown(throwable.message)
}
```

### Use case test

```kotlin
class GetManagerContactUCTest {
    private class FakeRepo(private val result: Result<ManagerContact>) : IPhoneAuthRepository {
        override suspend fun getManagerContact() = result.getOrThrow()
        override suspend fun checkNumber(request: RemoteRequest): NumberCheck = TODO()
    }

    private val contact = ManagerContact("Mona", "+201000000000", "m@x.com")

    @Test fun `success emits loading, success, loading false`() = runTest {
        val uc = GetManagerContactUC(FakeRepo(Result.success(contact)), passThroughErrorHandler, TestDispatchers(testScheduler))

        val results = uc(Unit).toList()

        assertEquals(Resource.Progress<ManagerContact>(true), results[0])
        assertEquals(Resource.Success(contact), results[1])
        assertEquals(Resource.Progress<ManagerContact>(false), results[2])
    }

    @Test fun `repo failure becomes Resource Failure`() = runTest {
        val uc = GetManagerContactUC(FakeRepo(Result.failure(IOException("no internet"))), passThroughErrorHandler, TestDispatchers(testScheduler))

        val results = uc(Unit).toList()

        assertTrue(results.any { it is Resource.Failure })
        assertEquals(Resource.Progress<ManagerContact>(false), results.last())
    }
}
```

### Request contract test

```kotlin
@Test fun `blank phone fails validation`() {
    val request = CheckNumberRequest("")
    assertThrows(AppException.Local.RequestValidation::class.java) { request.validateRequestContract() }
}
@Test fun `valid phone passes`() { CheckNumberRequest("+201000000000").validateRequestContract() }
```

### Mapper test

```kotlin
@Test fun `null data maps to empty strings`() {
    val domain = ManagerContactMapper().dtoToDomain(ManagerContactResponseDto(data = null))
    assertEquals("", domain.phone)
    assertFalse(domain.hasPhone)
}
```

### ViewModel test

```kotlin
class PhoneAuthViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    @Test fun `manager failure sets Failed`() = runTest {
        val dispatchers = TestDispatchers(testScheduler)
        val uc = GetManagerContactUC(FailingRepo(), passThroughErrorHandler, dispatchers)
        val vm = PhoneAuthViewModel(uc, /* other use cases */ dispatcherProvider = dispatchers)

        vm.processIntent(PhoneAuthAction.ContactManagerClicked)
        advanceUntilIdle()

        assertEquals(ManagerContactState.Failed, vm.currentViewState.manager)
    }
}
```

### Repository / network test with MockWebServer

Use the **same Gson converter as production** (a kotlinx.serialization converter in a test hides real parsing bugs).

```kotlin
class RetrofitNetworkProviderTest {
    private val server = MockWebServer()
    private lateinit var provider: RetrofitNetworkProvider

    @Before fun setUp() {
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        provider = RetrofitNetworkProvider(retrofit.create(ApiService::class.java))
    }
    @After fun tearDown() = server.shutdown()

    @Test fun `get parses the body into the dto and calls the right path`() = runTest {
        server.enqueue(MockResponse().setBody("""{"data":{"name":"Mona","phone":"+20100"}}"""))

        val dto = provider.get<ManagerContactResponseDto>(ManagerContactResponseDto::class.java, "manager/contact")

        assertEquals("Mona", dto.data?.name)
        assertEquals("/manager/contact", server.takeRequest().path)
    }

    @Test fun `http 401 throws HttpException`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        assertThrows(HttpException::class.java) {
            runBlocking { provider.get<ManagerContactResponseDto>(ManagerContactResponseDto::class.java, "manager/contact") }
        }
    }
}
```

Also test the `Authenticator` with MockWebServer (a 401, then a 200 after the session is cleared) and `SessionRepositoryImpl` with fakes for `TokenStore` and the preferences.

## 2. Compose previews and screenshots

Previews are how we prove every case works, and how reviewers check a screen without running the app. **Every screen is previewed in every state, theme and language, and the PR shows screenshots.**

### Rules

1. Screen composables are **stateless** (`state` in, `onAction` out), so a preview needs no ViewModel and no Koin.
2. Every `*Screen` composable has previews for **all states** through a `PreviewParameterProvider` over the state class.
3. Every preview uses the shared annotation below, so each state is rendered in **light/dark × every supported language (including RTL) × large font**.
4. Previews live next to the screen, are `private`, and use the app theme.
5. Konsist fails the build if a `*Screen.kt` file has no preview.

### Shared annotation (put it in the design system or `core.presentation`)

```kotlin
@Preview(name = "EN · Light", locale = "en", showBackground = true)
@Preview(name = "EN · Dark", locale = "en", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "AR · Light · RTL", locale = "ar", showBackground = true)
@Preview(name = "AR · Dark · RTL", locale = "ar", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Font 1.5x", fontScale = 1.5f, showBackground = true)
annotation class ThemeLanguagePreviews
```

Add one `@Preview(locale = …)` line for every additional supported language. Add `debugImplementation(libs.androidx.compose.ui.tooling)` so previews work.

### State provider and preview

```kotlin
class PhoneAuthStateProvider : PreviewParameterProvider<PhoneAuthState> {
    override val values = sequenceOf(
        PhoneAuthState(),                                                       // initial
        PhoneAuthState(number = "01000000000"),                                 // filled
        PhoneAuthState(isLoading = true),                                       // loading
        PhoneAuthState(error = AppException.Client.ResponseValidation(mapOf("phone" to "Invalid number"), "Invalid")),
        PhoneAuthState(manager = ManagerContactState.Failed),
    )
}

@ThemeLanguagePreviews
@Composable
private fun PhoneAuthScreenPreview(@PreviewParameter(PhoneAuthStateProvider::class) state: PhoneAuthState) {
    AppTheme { PhoneAuthScreen(state = state, onAction = {}) }
}
```

### Cases to cover

| Dimension | Cases |
|---|---|
| State | initial, filled, loading, content, empty, error (offline, server, validation per field), disabled, success |
| Theme | light, dark |
| Language | every supported language; at least one RTL |
| Text | long text and long words (wrapping, ellipsis), numbers and phone numbers in RTL |
| Font | default and 1.5× |
| Size | small phone, large phone (and tablet if supported) |
| Accessibility | content descriptions on icons/images, touch targets of at least 48dp |

### Screenshots for the PR

* Capture each **state × theme × language** from the preview panel (copy / export image) or from an emulator (`adb exec-out screencap -p > shot.png`).
* Name files `<screen>_<state>_<theme>_<lang>.png` and fill in the table of the PR template. Images live **in the PR**, not in the repo.
* No UI change: tick "Not a UI change" in the template.
* Later (optional): automated screenshot tests (Roborazzi, Paparazzi or the AGP preview screenshot tests) that record and verify these images in CI, so the previews become regression tests.

## 3. Instrumented tests

Instrumented tests run on a device/emulator (`./gradlew connectedDebugAndroidTest`; CI runs them on an API 35 emulator). They cover what unit tests cannot: real navigation, permissions, Android framework behaviour, and flows that cross screens or features.

### When is one required?

At least a happy path **and** one failure path when the PR:

* adds or changes a flow across **two or more screens** (phone number → OTP → home);
* connects **two features** (login then a protected call, booking then payment, visitor pass then QR);
* depends on **Android behaviour** (permissions, NFC, camera / QR, notifications, deep links, back stack, process restore);
* fixes a bug that only reproduces on a device.

Pure domain/data changes and single stateless composables do not need one (unit tests and previews cover them).

### Where they live

| Scope | Location | Rule |
|---|---|---|
| One screen of a feature | `<feature>/src/androidTest` | `createComposeRule()`, the screen's `Route`, fake data |
| A flow across screens or features | `app-*/src/androidTest` | `createAndroidComposeRule<MainActivity>()`, real navigation |

### Conventions

* Use **test tags** `"<screen>_<element>"` (`phone_auth_phone_input`, `phone_auth_send_code`); never find nodes by translated text.
* **No real network.** Start a `MockWebServer`, point the app's base URL at it by overriding the Retrofit / base-URL definition in a Koin test module (the override syntax depends on your Koin version).
* **Clean state per test:** clear the token store and preferences in `@Before`, shut the server down in `@After`.
* Results arrive asynchronously (the use case runs on IO), so wait with `waitUntil` instead of `Thread.sleep`.

### Skeleton

```kotlin
@RunWith(AndroidJUnit4::class)
class PhoneAuthFlowTest {

    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val server = MockWebServer()

    @Before fun setUp() {
        server.start()
        useMockBackend(server.url("/"))     // your helper: loads a Koin module overriding the base URL; clears session
    }

    @After fun tearDown() = server.shutdown()

    @Test fun validNumber_showsOtpStep() {
        server.enqueue(MockResponse().setBody("""{"data":{"name":"Mona","phone":"+201000000000"}}"""))

        compose.onNodeWithTag("phone_auth_phone_input").performTextInput("01000000000")
        compose.onNodeWithTag("phone_auth_send_code").performClick()

        compose.waitUntil(5_000) { compose.onAllNodesWithTag("phone_auth_otp_input").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("phone_auth_otp_input").assertIsDisplayed()
    }

    @Test fun serverError_showsErrorAndAllowsRetry() {
        server.enqueue(MockResponse().setResponseCode(500))

        compose.onNodeWithTag("phone_auth_phone_input").performTextInput("01000000000")
        compose.onNodeWithTag("phone_auth_send_code").performClick()

        compose.waitUntil(5_000) { compose.onAllNodesWithTag("phone_auth_error").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("phone_auth_retry").assertIsDisplayed()
    }
}
```

Dependencies (androidTest): `androidx.compose.ui:ui-test-junit4`, `androidx.test.ext:junit`, `mockwebserver`, `koin-test`; debug: `androidx.compose.ui:ui-test-manifest`.

### Integration of features

Examples: verify-OTP saves the session, then a protected call carries the token; a 401 clears the session and emits `SessionEvents.expired`; the app returns to login. These can also run as JVM tests with real use cases + repositories + `MockWebServer` (no UI) when the Android framework is not involved.

## 4. Static checks

Four tools run on every push (pre-push hook) and in CI. Fix, do not suppress.

### Spotless (formatting)

Formats Kotlin and Gradle Kotlin scripts. The `pre-commit` hook runs `./gradlew spotlessApply` and re-stages the files; CI runs `./gradlew spotlessCheck` and fails on any difference. Never hand-format or argue about style: let Spotless decide. Formatting-only changes use the `style:` commit type.

### Detekt (static analysis)

`./gradlew detekt` checks complexity, naming, style and potential bugs. Rules:

* New code must pass without adding to the baseline.
* Suppress a rule only on the smallest scope with a reason: `@Suppress("LongParameterList") // use case needs all four dependencies`.
* Consider the Compose rules plugin so composables follow the Compose API guidelines.
* The HTML report is uploaded by CI (`**/build/reports/detekt/`).

### Architecture tests (Konsist)

The `ArchitectureTest` (package `com.icompound.quality.konsist`) scans every package. Keep its rules in sync with these conventions:

| Rule | Why |
|---|---|
| `domain` does not import `data` or `presentation`; `presentation` does not import `data` | dependency rule |
| classes ending in `UC` extend `BaseUseCase` or `AuthenticatedUseCase` | error boundary |
| classes ending in `ViewModel` extend `MVIViewModel` | one state model |
| `I…Repository` interfaces live in `domain.repository`; implementations live in `data.repository` and implement them | layering |
| classes ending in `Dto` live in `data.dto` | layering |
| no `Dispatchers.IO/Main/Default` outside `DefaultDispatcherProvider` | testability |
| forbidden: `KoinComponent`, `runBlocking`, `GlobalScope`, `printStackTrace`, `android.util.Log` | conventions |

```kotlin
class ArchitectureTest {
    private val production = Konsist.scopeFromProduction()

    @Test fun `domain does not depend on data or presentation`() {
        production.files.filter { it.resideInPackage("..domain..") }.assertFalse {
            it.hasImport { import -> import.name.contains(".data.") || import.name.contains(".presentation.") }
        }
    }

    @Test fun `use cases extend a base use case`() {
        production.classes().withNameEndingWith("UC").assertTrue {
            it.hasParentWithName("BaseUseCase") || it.hasParentWithName("AuthenticatedUseCase")
        }
    }

    @Test fun `view models extend MVIViewModel`() {
        production.classes().withNameEndingWith("ViewModel").assertTrue { it.hasParentWithName("MVIViewModel") }
    }

    @Test fun `no forbidden patterns`() {
        val forbidden = listOf("KoinComponent", "runBlocking", "GlobalScope", "printStackTrace()", "android.util.Log")
        production.files.assertFalse { file -> forbidden.any { file.text.contains(it) } }
    }
}
```

(API names follow Konsist's usual style; adapt to your Konsist version. **Your test is the source of truth.**) When you add a convention, add a rule in the same PR.

Where it lives: the separate module **`:quality:konsist`** (run with `./gradlew :quality:konsist:test`; it is also part of `./gradlew test`). A dedicated module keeps cross-module scanning out of the features. CI and the pre-push hook run it.

Add this rule so previews cannot be forgotten:

```kotlin
@Test fun `every screen file declares previews`() {
    production.files.filter { it.name.endsWith("Screen") }.assertTrue {
        it.text.contains("@Preview") || it.text.contains("@ThemeLanguagePreviews")
    }
}
```

### Android Lint

Enable in the shared Gradle config:

```kotlin
android.lint {
    abortOnError = true
    warningsAsErrors = true
    checkDependencies = true
}
```

Custom lint rules worth adding (Konsist covers text patterns; lint covers resources and Android APIs):

* hard-coded user-visible strings
* `Log.*` instead of `AppLogger`
* `Toast`/`Snackbar` text not coming from resources
* `exception.message` shown in the UI

Skeleton of a custom detector that bans `android.util.Log`:

```kotlin
class LogUsageDetector : Detector(), Detector.UastScanner {
    override fun getApplicableMethodNames() = listOf("v", "d", "i", "w", "e")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (context.evaluator.isMemberInClass(method, "android.util.Log")) {
            context.report(ISSUE, node, context.getLocation(node), "Use AppLogger instead of android.util.Log")
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "UseAppLogger", briefDescription = "Use AppLogger", explanation = "Logging goes through AppLogger so release builds stay silent.",
            category = Category.CORRECTNESS, priority = 6, severity = Severity.ERROR,
            implementation = Implementation(LogUsageDetector::class.java, Scope.JAVA_FILE_SCOPE),
        )
    }
}
```

## 5. ProGuard / R8 rules

Gson and Retrofit read DTOs by reflection, so R8 must not rename or remove their fields. Each feature keeps its rules in its own `consumer-rules.pro` (they travel with the module):

```pro
# phone_auth/consumer-rules.pro
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# Gson DTOs: fields are set by reflection
-keep class com.icompound.phone_auth.data.dto.** { <fields>; }

# any field annotated with @SerializedName, wherever it lives
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# enums parsed by Gson
-keepclassmembers enum com.icompound.phone_auth.** { *; }
```

In `core/consumer-rules.pro`, keep models that are serialized with `toJson()` / `getModelFromJSON()` (for example `com.icompound.core.domain.model.User`).

R8 full mode needs generic signatures for Retrofit suspend functions:

```pro
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class retrofit2.Response
```

**Verify every feature:** build a minified variant (`isMinifyEnabled = true`), run the app, and exercise each call; or run an instrumented smoke test against `MockWebServer` on the minified build. A DTO that works in debug and returns empty data in release means a missing keep rule.

## 6. Before opening a PR

The pre-push hook runs the first line for you; CI runs everything.

```bash
./gradlew spotlessCheck detekt :quality:konsist:test test lint   # hooks / CI "validate"
./gradlew assembleRelease                                        # minified build still compiles
./gradlew connectedDebugAndroidTest                              # when an instrumented test is required
```

Then fill in the PR template with screenshots and ticks: [contributing.md](contributing.md#6-pull-requests).
