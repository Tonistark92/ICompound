import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}
// Also read local.properties, so keys placed there are picked up too
val localProperties = Properties()
project.rootProject.file("local.properties").takeIf { it.canRead() }?.let { file ->
    FileInputStream(file).use { localProperties.load(it) }
}
android {
    namespace = "com.icompound.core"
    compileSdk {
        version = release(37)
    }

    val properties = Properties()
    val configFile =
        listOf("config", "Config")
            .map { rootProject.file(it) }
            .firstOrNull { it.isFile && it.canRead() }

    configFile?.inputStream()?.use {
        properties.load(it)
    }

    // Looks in: config file -> local.properties -> gradle.properties / -P flag.
    // Fails the build with a clear message instead of silently using "".
    fun configValue(name: String): String {
        val raw =
            (
                properties.getProperty(name)
                    ?: localProperties.getProperty(name)
                    ?: providers.gradleProperty(name).orNull
            )?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw GradleException(
                    "$name is missing. Add $name=<value> to the config file, local.properties or gradle.properties",
                )
        return if (raw.startsWith("\"") && raw.endsWith("\"")) raw else "\"$raw\""
    }

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "API_KEY", configValue("API_KEY"))
        buildConfigField("String", "SOCKET_PORT", configValue("SOCKET_PORT"))
        buildConfigField("String", "GOOGLE_CLIENT_ID", configValue("GOOGLE_CLIENT_ID"))
        buildConfigField("String", "IC_COMPOUND_DATASTORE_NAME", configValue("IC_COMPOUND_DATASTORE_NAME"))
//        buildConfigField("String", "PIN_PRIMARY", "\"sha256/AAAA...=\"")
//        buildConfigField("String", "PIN_BACKUP",  "\"sha256/BBBB...=\"")

//        openssl s_client -servername api.icompound.com -connect api.icompound.com:443 </dev/null 2>/dev/null \
//        | openssl x509 -pubkey -noout \
//        | openssl pkey -pubin -outform der \
//        | openssl dgst -sha256 -binary | openssl enc -base64
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false

            buildConfigField("String", "REMOTE_URL", configValue("REMOTE_URL"))

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            buildConfigField("String", "REMOTE_URL", configValue("DEBUG_REMOTE_URL"))
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {

    implementation(project(":designsystem"))
    lintChecks(project(":quality:lint-rules"))
    // AndroidX
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material.icons)

    // Image Loading & Animations
    api(libs.coil.compose)
    implementation(libs.lottie.compose)
    implementation(libs.coil.network.okhttp)

    // Media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)

    // Google Sign In
    //noinspection LoginCredentials
    implementation(libs.androidx.credentials)
    //noinspection LoginCredentials
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Koin - Dependency Injection
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Preferences / DataStore
    implementation(libs.datastore.preferences.core)
    implementation(libs.datastore.preferences)

    // Retrofit / Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlin.coroutines.adapter)
    implementation(libs.gson)
    implementation(libs.logging.interceptor)
    testImplementation(libs.converter.gson)

    // Serialization
    api(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.converter)

    // Room Database
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    // Compose UI Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.paging.common)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(kotlin("test"))
}
