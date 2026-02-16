package eu.micer.distro.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppConfigTest {

    @Test
    fun `AppConfig should have default id of zero`() {
        // When
        val config = AppConfig(name = "Test", urlPattern = "https://example.com")

        // Then
        assertEquals(0L, config.id)
    }

    @Test
    fun `AppConfig should support custom id`() {
        // When
        val config = AppConfig(id = 42, name = "Test", urlPattern = "https://example.com")

        // Then
        assertEquals(42L, config.id)
    }

    @Test
    fun `AppConfig should require name and urlPattern`() {
        // When
        val config = AppConfig(name = "MyApp", urlPattern = "https://example.com/app")

        // Then
        assertEquals("MyApp", config.name)
        assertEquals("https://example.com/app", config.urlPattern)
    }

    @Test
    fun `AppConfig should have null optional fields by default`() {
        // When
        val config = AppConfig(name = "Test", urlPattern = "https://example.com")

        // Then
        assertEquals(null, config.packageName)
        assertEquals(null, config.versionName)
        assertEquals(null, config.versionCode)
        assertEquals(null, config.appLabel)
        assertEquals(null, config.quickLinks)
    }

    @Test
    fun `AppConfig should support all fields`() {
        // When
        val config = AppConfig(
            id = 1,
            name = "CompleteApp",
            urlPattern = "https://example.com/app-{version}.apk",
            packageName = "com.example.complete",
            versionName = "2.1.0",
            versionCode = 210,
            appLabel = "Complete Application",
            quickLinks = """[{"name":"Prod","link":"https://prod.com"}]"""
        )

        // Then
        assertEquals(1L, config.id)
        assertEquals("CompleteApp", config.name)
        assertEquals("https://example.com/app-{version}.apk", config.urlPattern)
        assertEquals("com.example.complete", config.packageName)
        assertEquals("2.1.0", config.versionName)
        assertEquals(210L, config.versionCode)
        assertEquals("Complete Application", config.appLabel)
        assertEquals("""[{"name":"Prod","link":"https://prod.com"}]""", config.quickLinks)
    }

    @Test
    fun `AppConfig copy should create equal object with same values`() {
        // Given
        val original = AppConfig(
            id = 1,
            name = "Original",
            urlPattern = "https://example.com",
            packageName = "com.example"
        )

        // When
        val copy = original.copy()

        // Then
        assertEquals(original.id, copy.id)
        assertEquals(original.name, copy.name)
        assertEquals(original.urlPattern, copy.urlPattern)
        assertEquals(original.packageName, copy.packageName)
    }

    @Test
    fun `AppConfig copy with changes should update specified fields`() {
        // Given
        val original = AppConfig(
            id = 1,
            name = "Original",
            urlPattern = "https://example.com/v1",
            packageName = "com.example.v1"
        )

        // When
        val updated = original.copy(
            name = "Updated",
            urlPattern = "https://example.com/v2",
            versionName = "2.0.0"
        )

        // Then
        assertEquals(1L, updated.id) // Unchanged
        assertEquals("Updated", updated.name) // Changed
        assertEquals("https://example.com/v2", updated.urlPattern) // Changed
        assertEquals("com.example.v1", updated.packageName) // Unchanged
        assertEquals("2.0.0", updated.versionName) // New field
    }

    @Test
    fun `AppConfig equals should return true for same data class instance`() {
        // Given
        val config1 = AppConfig(id = 1, name = "Test", urlPattern = "https://example.com")
        val config2 = AppConfig(id = 1, name = "Test", urlPattern = "https://example.com")

        // Then
        assertEquals(config1, config2)
    }

    @Test
    fun `AppConfig equals should return false for different values`() {
        // Given
        val config1 = AppConfig(id = 1, name = "Test1", urlPattern = "https://example.com/1")
        val config2 = AppConfig(id = 2, name = "Test2", urlPattern = "https://example.com/2")

        // Then
        assertNotEquals(config1, config2)
    }

    @Test
    fun `AppConfig hashCode should be consistent for equal objects`() {
        // Given
        val config1 = AppConfig(id = 1, name = "Test", urlPattern = "https://example.com")
        val config2 = AppConfig(id = 1, name = "Test", urlPattern = "https://example.com")

        // Then
        assertEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun `AppConfig toString should contain class name and field values`() {
        // Given
        val config = AppConfig(id = 1, name = "TestApp", urlPattern = "https://example.com")

        // When
        val str = config.toString()

        // Then
        assert(str.contains("AppConfig"))
        assert(str.contains("TestApp"))
        assert(str.contains("https://example.com"))
    }

    @Test
    fun `AppConfig component functions should destructure correctly`() {
        // Given
        val config = AppConfig(
            id = 1,
            name = "Test",
            urlPattern = "https://example.com",
            packageName = "com.test"
        )

        // When
        val (id, name, urlPattern, packageName) = config

        // Then
        assertEquals(1L, id)
        assertEquals("Test", name)
        assertEquals("https://example.com", urlPattern)
        assertEquals("com.test", packageName)
    }
}
