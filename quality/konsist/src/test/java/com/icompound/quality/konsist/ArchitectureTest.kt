package com.icompound.quality.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureTest {
    private val production = Konsist.scopeFromProduction()

    @Test
    fun `layers only depend downward`() {
        production.assertArchitecture {
            val domain = Layer("Domain", "com.icompound..domain..")
            val data = Layer("Data", "com.icompound..data..")
            val presentation = Layer("Presentation", "com.icompound..presentation..")

            domain.dependsOnNothing()
            data.dependsOn(domain)
            presentation.dependsOn(domain)
        }
    }

    @Test
    fun `use cases extend BaseUseCase`() {
        production
            .classes()
            .withNameEndingWith("UC")
            .assertTrue { it.hasParentWithName("BaseUseCase") }
    }

    @Test
    fun `view models extend MVIViewModel`() {
        production
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { it.hasParentWithName("MVIViewModel") }
    }

    @Test
    fun `repository implementations implement their domain interface`() {
        production
            .classes()
            .withNameEndingWith("RepositoryImpl")
            .withPackage("com.icompound..data..")
            .assertTrue { repository ->
                val expectedInterfaceName =
                    "I${repository.name.removeSuffix("RepositoryImpl")}Repository"

                repository.hasParentWithName(expectedInterfaceName)
            }
    }

    @Test
    fun `dtos are gson safe`() {
        production
            .classes()
            .withNameEndingWith("Dto")
            .properties()
            .assertTrue { it.hasAnnotationWithName("SerializedName") }
        // add a nullable-with-default check once you confirm the property API in your version
    }

    @Test
    fun `dispatchers are injected`() {
        production.files
            .filterNot { it.name == "DispatcherProvider" }
            .assertFalse { it.text.contains("Dispatchers.") }
    }

    @Test
    fun `debug forbidden production patterns`() {
        val forbiddenPatterns =
            listOf(
                "TODO(",
                "Log.d(",
                "Log.e(",
                "Log.i(",
                "Log.w(",
                "Log.v(",
                "Log.wtf(",
            )

        production.files.forEach { file ->

            val matches =
                forbiddenPatterns.filter { pattern ->
                    file.text.contains(pattern)
                }

            if (matches.isNotEmpty()) {
                println("--------------------------------")
                println("FILE: ${file.name}")
                println("PACKAGE: ${file.packagee?.name}")
                println("MATCHES: $matches")
            }
        }
    }
}
