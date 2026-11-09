plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.icompound.quality.konsist"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    testImplementation(libs.konsist)

    // Unit Testing
    testImplementation(libs.junit)

    // Instrumentation Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    implementation(kotlin("test"))
}

tasks.withType<Test>().configureEach {
    outputs.upToDateWhen { false }
    useJUnit()
}

tasks.register("konsistCheck") {
    group = "verification"
    description = "Runs ICompound architecture tests"

    dependsOn("testDebugUnitTest")
}