// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "com.diffplug.spotless")

    // ---------------------------------- detekt ----------------------------------
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        parallel = true
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        // One baseline per module. Generate with: ./gradlew detektBaseline
        // (copy config/detekt/baseline.xml to <module>/detekt-baseline.xml for an empty start)
        baseline = file("detekt-baseline.xml")
    }

    dependencies {
        add("detektPlugins", rootProject.libs.detekt.compose.rules)
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17" // match your project's JVM target
        reports {
            html.required.set(true)
            xml.required.set(true)
            sarif.required.set(true)
            txt.required.set(false)
        }
    }

    // Keep detekt's own embedded Kotlin compiler on the version it was built for,
    // so a newer Kotlin in your project does not break detekt itself.
    configurations.matching { it.name == "detekt" }.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
            }
        }
    }

    // ---------------------------------- spotless --------------------------------
    extensions.configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            targetExclude("**/build/**", "**/generated/**")
            ktlint(rootProject.libs.versions.ktlint.get()) // style comes from .editorconfig
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(rootProject.libs.versions.ktlint.get())
        }
        // Optional while adopting: only check files changed compared to main
        // ratchetFrom("origin/main")
    }
}
