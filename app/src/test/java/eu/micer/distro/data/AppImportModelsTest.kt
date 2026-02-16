package eu.micer.distro.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppImportModelsTest {

    @Test
    fun `QuickLink serialization should produce correct JSON`() {
        // Given
        val quickLink = QuickLink(name = "Production", link = "https://prod.example.com")

        // When
        val json = Json.encodeToString(quickLink)

        // Then
        assertEquals("""{"name":"Production","link":"https://prod.example.com"}""", json)
    }

    @Test
    fun `QuickLink deserialization should parse correct JSON`() {
        // Given
        val json = """{"name":"Beta","link":"https://beta.example.com"}"""

        // When
        val quickLink = Json.decodeFromString<QuickLink>(json)

        // Then
        assertEquals("Beta", quickLink.name)
        assertEquals("https://beta.example.com", quickLink.link)
    }

    @Test
    fun `AppImportItem serialization should handle all fields`() {
        // Given
        val item = AppImportItem(
            name = "MyApp",
            urlPattern = "https://example.com/app-{version}.apk",
            packageName = "com.example.app",
            quickLinks = listOf(
                QuickLink("Stable", "https://example.com/stable.apk"),
                QuickLink("Beta", "https://example.com/beta.apk")
            )
        )

        // When
        val json = Json.encodeToString(item)

        // Then
        assertEquals(
            """{"name":"MyApp","urlPattern":"https://example.com/app-{version}.apk","packageName":"com.example.app","quickLinks":[{"name":"Stable","link":"https://example.com/stable.apk"},{"name":"Beta","link":"https://example.com/beta.apk"}]}""",
            json
        )
    }

    @Test
    fun `AppImportItem deserialization should handle all fields`() {
        // Given
        val json = """{"name":"FullApp","urlPattern":"https://example.com/app.apk","packageName":"com.full.app","quickLinks":[{"name":"Link1","link":"https://link1.com"}]}"""

        // When
        val item = Json.decodeFromString<AppImportItem>(json)

        // Then
        assertEquals("FullApp", item.name)
        assertEquals("https://example.com/app.apk", item.urlPattern)
        assertEquals("com.full.app", item.packageName)
        assertEquals(1, item.quickLinks.size)
        assertEquals("Link1", item.quickLinks[0].name)
    }

    @Test
    fun `AppImportItem deserialization should handle missing optional fields`() {
        // Given
        val json = """{"name":"MinimalApp","urlPattern":"https://example.com/app.apk"}"""

        // When
        val item = Json.decodeFromString<AppImportItem>(json)

        // Then
        assertEquals("MinimalApp", item.name)
        assertEquals("https://example.com/app.apk", item.urlPattern)
        assertNull(item.packageName)
        assertEquals(emptyList<QuickLink>(), item.quickLinks)
    }

    @Test
    fun `AppImportItem deserialization should handle null packageName`() {
        // Given
        val json = """{"name":"NoPkgApp","urlPattern":"https://example.com/app.apk","packageName":null}"""

        // When
        val item = Json.decodeFromString<AppImportItem>(json)

        // Then
        assertNull(item.packageName)
    }

    @Test
    fun `AppImportList serialization should produce correct JSON`() {
        // Given
        val importList = AppImportList(
            version = "1.0",
            apps = listOf(
                AppImportItem(name = "App1", urlPattern = "https://example.com/1"),
                AppImportItem(name = "App2", urlPattern = "https://example.com/2")
            )
        )

        // When
        val json = Json.encodeToString(importList)

        // Then - Kotlin serialization omits default values, so empty quickLinks is not included
        assertEquals(
            """{"version":"1.0","apps":[{"name":"App1","urlPattern":"https://example.com/1"},{"name":"App2","urlPattern":"https://example.com/2"}]}""",
            json
        )
    }

    @Test
    fun `AppImportList deserialization should parse correct JSON`() {
        // Given
        val json = """{"version":"2.0","apps":[{"name":"App1","urlPattern":"https://example.com/1","quickLinks":[]}]}"""

        // When
        val importList = Json.decodeFromString<AppImportList>(json)

        // Then
        assertEquals("2.0", importList.version)
        assertEquals(1, importList.apps.size)
        assertEquals("App1", importList.apps[0].name)
    }

    @Test
    fun `toAppConfig should convert AppImportItem correctly with all fields`() {
        // Given
        val importItem = AppImportItem(
            name = "TestApp",
            urlPattern = "https://example.com/app-{version}.apk",
            packageName = "com.example.app",
            quickLinks = listOf(
                QuickLink("Stable", "https://example.com/stable.apk")
            )
        )

        // When
        val config = importItem.toAppConfig()

        // Then
        assertEquals(0, config.id) // Default auto-generated id
        assertEquals("TestApp", config.name)
        assertEquals("https://example.com/app-{version}.apk", config.urlPattern)
        assertEquals("com.example.app", config.packageName)
        assertEquals("""[{"name":"Stable","link":"https://example.com/stable.apk"}]""", config.quickLinks)
        assertNull(config.versionName)
        assertNull(config.versionCode)
        assertNull(config.appLabel)
    }

    @Test
    fun `toAppConfig should handle blank package name`() {
        // Given
        val importItem = AppImportItem(
            name = "TestApp",
            urlPattern = "https://example.com/app.apk",
            packageName = "  ", // Blank with spaces
            quickLinks = emptyList()
        )

        // When
        val config = importItem.toAppConfig()

        // Then
        assertNull(config.packageName) // Should be nullified
    }

    @Test
    fun `toAppConfig should handle null package name`() {
        // Given
        val importItem = AppImportItem(
            name = "TestApp",
            urlPattern = "https://example.com/app.apk",
            packageName = null,
            quickLinks = emptyList()
        )

        // When
        val config = importItem.toAppConfig()

        // Then
        assertNull(config.packageName)
    }

    @Test
    fun `toAppConfig should trim package name`() {
        // Given
        val importItem = AppImportItem(
            name = "TestApp",
            urlPattern = "https://example.com/app.apk",
            packageName = "  com.example.app  ", // With leading/trailing spaces
            quickLinks = emptyList()
        )

        // When
        val config = importItem.toAppConfig()

        // Then
        assertEquals("com.example.app", config.packageName) // Should be trimmed
    }

    @Test
    fun `quickLinksToJson should return empty string for empty list`() {
        // Given
        val emptyList = emptyList<QuickLink>()

        // When
        val result = quickLinksToJson(emptyList)

        // Then
        assertEquals("", result)
    }

    @Test
    fun `quickLinksToJson should serialize single link correctly`() {
        // Given
        val links = listOf(QuickLink("Single", "https://single.com"))

        // When
        val result = quickLinksToJson(links)

        // Then
        assertEquals("""[{"name":"Single","link":"https://single.com"}]""", result)
    }

    @Test
    fun `quickLinksToJson should serialize multiple links correctly`() {
        // Given
        val links = listOf(
            QuickLink("Link1", "https://1.com"),
            QuickLink("Link2", "https://2.com")
        )

        // When
        val result = quickLinksToJson(links)

        // Then
        assertEquals("""[{"name":"Link1","link":"https://1.com"},{"name":"Link2","link":"https://2.com"}]""", result)
    }

    @Test
    fun `quickLinksFromJson should return empty list for null`() {
        // When
        val result = quickLinksFromJson(null)

        // Then
        assertEquals(emptyList<QuickLink>(), result)
    }

    @Test
    fun `quickLinksFromJson should return empty list for blank string`() {
        // When
        val result = quickLinksFromJson("   ")

        // Then
        assertEquals(emptyList<QuickLink>(), result)
    }

    @Test
    fun `quickLinksFromJson should return empty list for empty string`() {
        // When
        val result = quickLinksFromJson("")

        // Then
        assertEquals(emptyList<QuickLink>(), result)
    }

    @Test
    fun `quickLinksFromJson should return empty list for invalid JSON`() {
        // When
        val result = quickLinksFromJson("not valid json")

        // Then
        assertEquals(emptyList<QuickLink>(), result)
    }

    @Test
    fun `quickLinksFromJson should return empty list for malformed JSON array`() {
        // When
        val result = quickLinksFromJson("{\"invalid\": \"structure\"}")

        // Then
        assertEquals(emptyList<QuickLink>(), result)
    }

    @Test
    fun `quickLinksFromJson should parse valid JSON with single link`() {
        // Given
        val json = """[{"name":"Test","link":"https://test.com"}]"""

        // When
        val result = quickLinksFromJson(json)

        // Then
        assertEquals(1, result.size)
        assertEquals("Test", result[0].name)
        assertEquals("https://test.com", result[0].link)
    }

    @Test
    fun `quickLinksFromJson should parse valid JSON with multiple links`() {
        // Given
        val json = """[{"name":"First","link":"https://first.com"},{"name":"Second","link":"https://second.com"}]"""

        // When
        val result = quickLinksFromJson(json)

        // Then
        assertEquals(2, result.size)
        assertEquals("First", result[0].name)
        assertEquals("Second", result[1].name)
    }

    @Test
    fun `quickLinks round trip should preserve data`() {
        // Given
        val original = listOf(
            QuickLink("Alpha", "https://alpha.example.com"),
            QuickLink("Beta", "https://beta.example.com")
        )

        // When
        val json = quickLinksToJson(original)
        val recovered = quickLinksFromJson(json)

        // Then
        assertEquals(original, recovered)
    }

    @Test
    fun `quickLinks round trip with special characters should preserve data`() {
        // Given
        val original = listOf(
            QuickLink("Link with spaces", "https://example.com/path with spaces/query?param=value&other=test")
        )

        // When
        val json = quickLinksToJson(original)
        val recovered = quickLinksFromJson(json)

        // Then
        assertEquals(1, recovered.size)
        assertEquals("Link with spaces", recovered[0].name)
        assertEquals("https://example.com/path with spaces/query?param=value&other=test", recovered[0].link)
    }
}
