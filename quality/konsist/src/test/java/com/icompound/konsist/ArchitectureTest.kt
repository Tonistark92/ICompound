package com.icompound.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureTest {

    @Test
    fun `use cases extend BaseUseCase`() =
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("UC")
            .assertTrue {
                it.hasParentWithName("BaseUseCase")
            }

    @Test
    fun `view models extend BaseViewModel`() =
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue {
                it.hasParentWithName("BaseViewModel")
            }

    @Test
    fun `domain does not import data or presentation`() =
        Konsist.scopeFromProject()
            .files
            .withPackage("..domain..")
            .assertFalse {
                it.hasImport { import ->
                    import.hasNameContaining(".data.") ||
                            import.hasNameContaining(".presentation.")
                }
            }

    @Test
    fun `viewmodels never reference ApiService or INetworkProvider`() =
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertFalse {
                it.containingFile.hasImport { import ->
                    import.hasNameContaining("ApiService") ||
                            import.hasNameContaining("INetworkProvider")
                }
            }

    @Test
    fun `dtos live in a dto package and extend BaseDto`() =
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("Dto")
            .assertTrue {
                it.resideInPackage("..data.dto..") &&
                        it.hasParentWithName("BaseDto")
            }
}
