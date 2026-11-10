
plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    // Android Lint API
    compileOnly("com.android.tools.lint:lint-api:32.5.1")

    // Lint detector tests
    testImplementation("com.android.tools.lint:lint-tests:32.5.1")

    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
    outputs.upToDateWhen { false }
}

tasks.jar {
    manifest {
        attributes("Lint-Registry-v2" to "com.icompound.quality.lint_rules.ICompoundIssueRegistry")
    }
}
