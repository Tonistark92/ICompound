plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {
    compileOnly(libs.android.lint.api)

    testImplementation(libs.android.lint.tests)
    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
}


dependencies {
    testImplementation(libs.junit)
    // Android Lint API
    compileOnly("com.android.tools.lint:lint-api:32.5.0")

    // Lint testing (optional, if you're writing detector tests)
    testImplementation("com.android.tools.lint:lint-tests:31.13.2")
}
