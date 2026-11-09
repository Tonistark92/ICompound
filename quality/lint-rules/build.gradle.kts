plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.icompound.quality.lint_rules"
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
    implementation(libs.lint)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Android Lint API
    compileOnly("com.android.tools.lint:lint-api:31.+")

    // Lint testing (optional, if you're writing detector tests)
    testImplementation("com.android.tools.lint:lint-tests:31.+")
}