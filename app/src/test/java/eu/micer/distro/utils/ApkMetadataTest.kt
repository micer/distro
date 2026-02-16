package eu.micer.distro.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkMetadataTest {

    @Test
    fun `ApkMetadata should hold all fields`() {
        // Given
        val packageName = "com.example.app"
        val versionName = "2.1.0"
        val versionCode = 210L
        val appLabel = "Example Application"

        // When
        val metadata = ApkMetadata(
            packageName = packageName,
            versionName = versionName,
            versionCode = versionCode,
            appLabel = appLabel
        )

        // Then
        assertEquals(packageName, metadata.packageName)
        assertEquals(versionName, metadata.versionName)
        assertEquals(versionCode, metadata.versionCode)
        assertEquals(appLabel, metadata.appLabel)
    }

    @Test
    fun `ApkMetadata should support minimal data`() {
        // Given & When
        val metadata = ApkMetadata(
            packageName = "com.minimal",
            versionName = "1.0",
            versionCode = 1L,
            appLabel = "Minimal"
        )

        // Then
        assertEquals("com.minimal", metadata.packageName)
        assertEquals("1.0", metadata.versionName)
        assertEquals(1L, metadata.versionCode)
        assertEquals("Minimal", metadata.appLabel)
    }

    @Test
    fun `ApkMetadata copy should create equal object`() {
        // Given
        val original = ApkMetadata(
            packageName = "com.test",
            versionName = "1.0.0",
            versionCode = 100L,
            appLabel = "Test App"
        )

        // When
        val copy = original.copy()

        // Then
        assertEquals(original.packageName, copy.packageName)
        assertEquals(original.versionName, copy.versionName)
        assertEquals(original.versionCode, copy.versionCode)
        assertEquals(original.appLabel, copy.appLabel)
        assertEquals(original, copy)
    }

    @Test
    fun `ApkMetadata copy with changes should update specified fields only`() {
        // Given
        val original = ApkMetadata(
            packageName = "com.original",
            versionName = "1.0.0",
            versionCode = 100L,
            appLabel = "Original App"
        )

        // When
        val updated = original.copy(
            versionName = "2.0.0",
            versionCode = 200L
        )

        // Then
        assertEquals("com.original", updated.packageName) // Unchanged
        assertEquals("2.0.0", updated.versionName) // Changed
        assertEquals(200L, updated.versionCode) // Changed
        assertEquals("Original App", updated.appLabel) // Unchanged
    }

    @Test
    fun `ApkMetadata equals should return true for same values`() {
        // Given
        val metadata1 = ApkMetadata(
            packageName = "com.test",
            versionName = "1.0.0",
            versionCode = 100L,
            appLabel = "Test"
        )
        val metadata2 = ApkMetadata(
            packageName = "com.test",
            versionName = "1.0.0",
            versionCode = 100L,
            appLabel = "Test"
        )

        // Then
        assertEquals(metadata1, metadata2)
    }

    @Test
    fun `ApkMetadata hashCode should be consistent for equal objects`() {
        // Given
        val metadata1 = ApkMetadata(
            packageName = "com.test",
            versionName = "1.0.0",
            versionCode = 100L,
            appLabel = "Test"
        )
        val metadata2 = ApkMetadata(
            packageName = "com.test",
            versionName = "1.0.0",
            versionCode = 100L,
            appLabel = "Test"
        )

        // Then
        assertEquals(metadata1.hashCode(), metadata2.hashCode())
    }

    @Test
    fun `ApkMetadata toString should contain relevant information`() {
        // Given
        val metadata = ApkMetadata(
            packageName = "com.example",
            versionName = "1.0.0",
            versionCode = 100L,
            appLabel = "Example"
        )

        // When
        val str = metadata.toString()

        // Then
        assert(str.contains("ApkMetadata"))
        assert(str.contains("com.example"))
        assert(str.contains("1.0.0"))
    }

    @Test
    fun `ApkMetadata component functions should destructure correctly`() {
        // Given
        val metadata = ApkMetadata(
            packageName = "com.test",
            versionName = "1.0.0",
            versionCode = 100L,
            appLabel = "Test App"
        )

        // When
        val (pkg, verName, verCode, label) = metadata

        // Then
        assertEquals("com.test", pkg)
        assertEquals("1.0.0", verName)
        assertEquals(100L, verCode)
        assertEquals("Test App", label)
    }

    @Test
    fun `ApkMetadata should handle empty strings`() {
        // Given & When
        val metadata = ApkMetadata(
            packageName = "",
            versionName = "",
            versionCode = 0L,
            appLabel = ""
        )

        // Then
        assertEquals("", metadata.packageName)
        assertEquals("", metadata.versionName)
        assertEquals(0L, metadata.versionCode)
        assertEquals("", metadata.appLabel)
    }

    @Test
    fun `ApkMetadata should handle large version codes`() {
        // Given
        val largeVersionCode = Long.MAX_VALUE

        // When
        val metadata = ApkMetadata(
            packageName = "com.test",
            versionName = "999.999.999",
            versionCode = largeVersionCode,
            appLabel = "Test"
        )

        // Then
        assertEquals(largeVersionCode, metadata.versionCode)
    }
}
