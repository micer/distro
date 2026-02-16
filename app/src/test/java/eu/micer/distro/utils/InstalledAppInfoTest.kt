package eu.micer.distro.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstalledAppInfoTest {

    @Test
    fun `InstalledAppInfo with not installed status`() {
        // Given & When
        val info = InstalledAppInfo(isInstalled = false)

        // Then
        assertFalse(info.isInstalled)
        assertNull(info.versionName)
        assertNull(info.versionCode)
    }

    @Test
    fun `InstalledAppInfo with installed status and version info`() {
        // Given
        val versionName = "2.1.0"
        val versionCode = 210L

        // When
        val info = InstalledAppInfo(
            isInstalled = true,
            versionName = versionName,
            versionCode = versionCode
        )

        // Then
        assertTrue(info.isInstalled)
        assertEquals(versionName, info.versionName)
        assertEquals(versionCode, info.versionCode)
    }

    @Test
    fun `InstalledAppInfo default values for optional fields`() {
        // Given & When - only provide isInstalled
        val info = InstalledAppInfo(isInstalled = true)

        // Then - versionName and versionCode should default to null
        assertTrue(info.isInstalled)
        assertNull(info.versionName)
        assertNull(info.versionCode)
    }

    @Test
    fun `InstalledAppInfo copy should create equal object`() {
        // Given
        val original = InstalledAppInfo(
            isInstalled = true,
            versionName = "1.0.0",
            versionCode = 100L
        )

        // When
        val copy = original.copy()

        // Then
        assertEquals(original.isInstalled, copy.isInstalled)
        assertEquals(original.versionName, copy.versionName)
        assertEquals(original.versionCode, copy.versionCode)
        assertEquals(original, copy)
    }

    @Test
    fun `InstalledAppInfo copy with changes should update specified fields only`() {
        // Given
        val original = InstalledAppInfo(
            isInstalled = true,
            versionName = "1.0.0",
            versionCode = 100L
        )

        // When
        val updated = original.copy(
            isInstalled = false,
            versionName = null
        )

        // Then
        assertFalse(updated.isInstalled) // Changed
        assertNull(updated.versionName) // Changed
        assertEquals(100L, updated.versionCode) // Unchanged
    }

    @Test
    fun `InstalledAppInfo equals should return true for same values`() {
        // Given
        val info1 = InstalledAppInfo(
            isInstalled = true,
            versionName = "1.0.0",
            versionCode = 100L
        )
        val info2 = InstalledAppInfo(
            isInstalled = true,
            versionName = "1.0.0",
            versionCode = 100L
        )

        // Then
        assertEquals(info1, info2)
    }

    @Test
    fun `InstalledAppInfo equals should return false for different isInstalled`() {
        // Given
        val info1 = InstalledAppInfo(isInstalled = true, versionName = "1.0", versionCode = 1L)
        val info2 = InstalledAppInfo(isInstalled = false, versionName = "1.0", versionCode = 1L)

        // Then
        assertFalse(info1 == info2)
    }

    @Test
    fun `InstalledAppInfo equals should return false for different versionName`() {
        // Given
        val info1 = InstalledAppInfo(isInstalled = true, versionName = "1.0", versionCode = 1L)
        val info2 = InstalledAppInfo(isInstalled = true, versionName = "2.0", versionCode = 1L)

        // Then
        assertFalse(info1 == info2)
    }

    @Test
    fun `InstalledAppInfo hashCode should be consistent for equal objects`() {
        // Given
        val info1 = InstalledAppInfo(
            isInstalled = true,
            versionName = "1.0.0",
            versionCode = 100L
        )
        val info2 = InstalledAppInfo(
            isInstalled = true,
            versionName = "1.0.0",
            versionCode = 100L
        )

        // Then
        assertEquals(info1.hashCode(), info2.hashCode())
    }

    @Test
    fun `InstalledAppInfo component functions should destructure correctly`() {
        // Given
        val info = InstalledAppInfo(
            isInstalled = true,
            versionName = "1.0.0",
            versionCode = 100L
        )

        // When
        val (installed, verName, verCode) = info

        // Then
        assertTrue(installed)
        assertEquals("1.0.0", verName)
        assertEquals(100L, verCode)
    }

    @Test
    fun `InstalledAppInfo toString should contain class name and field values`() {
        // Given
        val info = InstalledAppInfo(
            isInstalled = true,
            versionName = "1.0.0",
            versionCode = 100L
        )

        // When
        val str = info.toString()

        // Then
        assert(str.contains("InstalledAppInfo"))
        assert(str.contains("true")) // isInstalled
    }

    @Test
    fun `InstalledAppInfo should handle empty versionName`() {
        // Given & When
        val info = InstalledAppInfo(
            isInstalled = true,
            versionName = "",
            versionCode = 0L
        )

        // Then
        assertEquals("", info.versionName)
        assertEquals(0L, info.versionCode)
    }

    @Test
    fun `InstalledAppInfo should handle large version codes`() {
        // Given
        val largeVersionCode = Long.MAX_VALUE

        // When
        val info = InstalledAppInfo(
            isInstalled = true,
            versionName = "999.999.999",
            versionCode = largeVersionCode
        )

        // Then
        assertEquals(largeVersionCode, info.versionCode)
    }
}
