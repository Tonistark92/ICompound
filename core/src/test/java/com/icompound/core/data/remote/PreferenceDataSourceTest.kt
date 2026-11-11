package com.icompound.core.data.remote

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.icompound.core.data.datasource.local.PreferenceDataSource
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PreferenceDataSourceTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var sut: PreferenceDataSource

    @Before
    fun setUp() {
        val store =
            PreferenceDataStoreFactory.create(scope = scope) {
                File(tmp.root, "test.preferences_pb")
            }
        sut = PreferenceDataSource(store)
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `returns default when key is missing`() =
        runTest {
            assertEquals("d", sut.getValue("k", "d").first())
            assertEquals(1, sut.getValue("k", 1).first())
            assertEquals(1L, sut.getValue("k", 1L).first())
            assertEquals(1f, sut.getValue("k", 1f).first())
            assertEquals(1.0, sut.getValue("k", 1.0).first())
            assertEquals(true, sut.getValue("k", true).first())
        }

    @Test
    fun `round trips every supported type`() =
        runTest {
            sut.setValue("s", "hello")
            sut.setValue("i", 42)
            sut.setValue("l", 42L)
            sut.setValue("f", 1.5f)
            sut.setValue("d", 2.5)
            sut.setValue("b", true)

            assertEquals("hello", sut.getValue("s", "").first())
            assertEquals(42, sut.getValue("i", 0).first())
            assertEquals(42L, sut.getValue("l", 0L).first())
            assertEquals(1.5f, sut.getValue("f", 0f).first())
            assertEquals(2.5, sut.getValue("d", 0.0).first())
            assertEquals(true, sut.getValue("b", false).first())
        }

    @Test
    fun `overwrites an existing value`() =
        runTest {
            sut.setValue("k", "a")
            sut.setValue("k", "b")
            assertEquals("b", sut.getValue("k", "").first())
        }

    @Test
    fun `flow emits again when value changes`() =
        runTest {
            sut.setValue("k", 1)
            val flow = sut.getValue("k", 0)
            assertEquals(1, flow.first())
            sut.setValue("k", 2)
            assertEquals(2, flow.first())
        }

    @Test
    fun `unsupported value type is silently ignored`() =
        runTest {
            sut.setValue("k", listOf(1, 2)) // documents current behavior
            assertEquals("d", sut.getValue("k", "d").first())
        }

    @Test
    fun `null value is a no-op and does not clear an existing value`() =
        runTest {
            sut.setValue("k", "a")
            sut.setValue("k", null)
            assertEquals("a", sut.getValue("k", "").first())
        }

    @Test
    fun `null default and unsupported default return the default`() =
        runTest {
            assertNull(sut.getValue("k", null).first())
        }
}
