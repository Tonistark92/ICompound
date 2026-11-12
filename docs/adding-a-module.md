# Adding a module

Three cases: a new **feature module**, a new **app**, and a new **shared (core) component**. Gradle snippets use placeholders (`libs.…`, SDK versions): copy the conventions from an existing module in the repo.

## A. New feature module

Use this when a business area gets its own Gradle module (`:maintenance`, `:billing`, …). If features are plain packages in your project, only steps 4, 6 and 7 apply.

### Steps

1. **Create the module** `maintenance/` and add it to `settings.gradle.kts`:
   ```kotlin
   include(":core", ":phone_auth", ":maintenance", ":app-admin", ":app-resident")
   ```
2. **`maintenance/build.gradle.kts`** (template):
   ```kotlin
   plugins {
       alias(libs.plugins.android.library)
       alias(libs.plugins.kotlin.android)
       alias(libs.plugins.kotlin.compose)          // only if the feature has screens
   }

   android {
       namespace = "com.icompound.maintenance"
       compileSdk = libs.versions.compileSdk.get().toInt()
       defaultConfig {
           minSdk = libs.versions.minSdk.get().toInt()
           consumerProguardFiles("consumer-rules.pro")
       }
       buildFeatures { compose = true }
   }

   dependencies {
       implementation(project(":core"))            // the ONLY project dependency a feature may have
       implementation(libs.koin.android)
       implementation(libs.koin.androidx.compose)
       // compose, lifecycle, … (same aliases as the other features)

       testImplementation(libs.junit)
       testImplementation(libs.kotlinx.coroutines.test)
       testImplementation(libs.mockwebserver)
       testImplementation(libs.mockk)
   }
   ```
   A feature **never** declares `project(":other_feature")`.
3. **Add `consumer-rules.pro`** (empty file at first; see [testing-and-quality.md](testing-and-quality.md#5-proguard--r8-rules) for the DTO rules).
4. **Create the package skeleton** `com.icompound.maintenance/{data,domain,presentation,di}` and follow [adding-a-feature.md](adding-a-feature.md).
5. **Expose the Koin module:** `val maintenanceModule` must be **public**. Classes marked `internal` are invisible outside the module; that is why the module has to expose its own Koin module.
6. **Wire it into each app that uses it:**
   ```kotlin
   // app build.gradle.kts
   implementation(project(":maintenance"))
   // Application.onCreate → startKoin { modules(coreDiModule, phoneAuthModule, maintenanceModule) }
   // navigation graph → composable("maintenance") { MaintenanceRoute(...) }
   ```
7. **Tell the quality checks:** the Konsist tests (`:quality:konsist`) scan the whole project, so a new module is covered automatically **as long as it follows the naming rules**. If the test lists modules or packages explicitly, add the new one. Detekt, Spotless and lint pick the module up through the root Gradle configuration; CI needs no change.
8. **Tests first run:** `./gradlew :maintenance:test`.

### Checklist

- [ ] module in `settings.gradle.kts`, `namespace` set
- [ ] depends only on `:core`
- [ ] four packages (`data`, `domain`, `presentation`, `di`)
- [ ] public `maintenanceModule`, added to every app that needs it
- [ ] `consumer-rules.pro` present
- [ ] tests, Konsist, lint green

## B. New app

1. Create `app-<name>/` (Android application module, `applicationId` `com.icompound.<name>`), add it to `settings.gradle.kts`.
2. Dependencies: `project(":core")` plus the features it needs. **Never** another app.
3. Application class:
   ```kotlin
   class IcompoundXxxApp : Application() {
       override fun onCreate() {
           super.onCreate()
           startKoin {
               androidContext(this@IcompoundXxxApp)
               modules(coreDiModule /*, one module per feature the app uses */)
           }
       }
   }
   ```
4. `AndroidManifest.xml`: register the Application class, `INTERNET` permission, `android:allowBackup="false"`, network security config.
5. Navigation host: one destination per feature `Route`; the app passes navigation callbacks into the features (features never navigate to each other).
6. Collect `SessionEvents.expired` once at the root and navigate to login, clearing the back stack.
7. Theme: use the shared design system (or a copy of the theme tokens until `designsystem` exists).
8. Build config: `buildConfigField`s (`BASE_URL`, pins, keys) per build type / flavor, read from `local.properties` or CI secrets. Details: [security-and-release.md](security-and-release.md).
9. Signing and release: configure in CI; never commit keystores.
10. Smoke test: build, log in, call one protected endpoint, trigger a 401.

## C. New shared component in `core`

Changes to `core` affect every feature, so:

1. First ask: is it needed by two or more features? If only one needs it, keep it in that feature.
2. Put it in the matching `core` package ([project-structure.md](project-structure.md#2-core-packages)). Domain code must not import data or presentation.
3. Constructor-inject its dependencies; no `KoinComponent`, no `runBlocking`, no direct dispatchers.
4. Register it in the right `core` Koin module.
5. Add unit tests.
6. Update [core-components.md](../../../Downloads/core-components.md) and, if it introduces a rule (naming, parent class), the Konsist `ArchitectureTest`.
7. Announce breaking changes in the PR description.
