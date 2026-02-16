package eu.micer.distro.viewmodel

import eu.micer.distro.data.AppConfig
import eu.micer.distro.utils.DownloadState
import eu.micer.distro.utils.InstalledAppInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppViewModelTest {

    @Test
    fun `ImportState Idle should be singleton`() {
        assertTrue(ImportState.Idle === ImportState.Idle)
    }

    @Test
    fun `ImportState Loading should be singleton`() {
        assertTrue(ImportState.Loading === ImportState.Loading)
    }

    @Test
    fun `ImportState Success should hold count`() {
        val success = ImportState.Success(count = 42)
        assertEquals(42, success.count)
    }

    @Test
    fun `ImportState Error should hold message`() {
        val error = ImportState.Error(message = "Something went wrong")
        assertEquals("Something went wrong", error.message)
    }

    @Test
    fun `AppConfigWithStatus should hold config and installed info`() {
        val config = AppConfig(id = 1, name = "Test", urlPattern = "https://example.com")
        val installedInfo = InstalledAppInfo(isInstalled = true, versionName = "1.0.0", versionCode = 100)

        val withStatus = AppConfigWithStatus(config = config, installedInfo = installedInfo)

        assertEquals("Test", withStatus.config.name)
        assertTrue(withStatus.installedInfo.isInstalled)
        assertEquals("1.0.0", withStatus.installedInfo.versionName)
        assertEquals(100L, withStatus.installedInfo.versionCode)
    }

    @Test
    fun `AppConfigWithStatus should handle not installed`() {
        val config = AppConfig(id = 1, name = "NotInstalled", urlPattern = "https://example.com")
        val installedInfo = InstalledAppInfo(isInstalled = false)

        val withStatus = AppConfigWithStatus(config = config, installedInfo = installedInfo)

        assertEquals("NotInstalled", withStatus.config.name)
        assertFalse(withStatus.installedInfo.isInstalled)
        assertEquals(null, withStatus.installedInfo.versionName)
        assertEquals(null, withStatus.installedInfo.versionCode)
    }

    @Test
    fun `BulkDownloadItem should hold all fields`() {
        val item = BulkDownloadItem(
            appId = 1,
            appName = "TestApp",
            packageName = "com.test",
            urlPattern = "https://example.com/app.apk",
            versionName = "1.0",
            state = DownloadState.Idle,
            order = 0
        )

        assertEquals(1L, item.appId)
        assertEquals("TestApp", item.appName)
        assertEquals("com.test", item.packageName)
        assertEquals("https://example.com/app.apk", item.urlPattern)
        assertEquals("1.0", item.versionName)
        assertEquals(0, item.order)
    }

    @Test
    fun `BulkDownloadItem copy should work`() {
        val item = BulkDownloadItem(
            appId = 1,
            appName = "Test",
            packageName = null,
            urlPattern = "https://example.com",
            versionName = "1.0",
            state = DownloadState.Idle,
            order = 0
        )

        val downloading = DownloadState.Downloading(0.5f)
        val updated = item.copy(state = downloading)

        assertEquals("Test", updated.appName)
        assertTrue(updated.state is DownloadState.Downloading)
    }

    @Test
    fun `BulkDownloadState should have correct default values`() {
        val state = BulkDownloadState()

        assertEquals(emptyList<BulkDownloadItem>(), state.items)
        assertFalse(state.isActive)
        assertEquals(0, state.completedCount)
        assertEquals(0, state.failedCount)
        assertEquals(0, state.totalCount)
        assertEquals(0L, state.completedAt)
    }

    @Test
    fun `BulkDownloadState should support custom values`() {
        val items = listOf(
            BulkDownloadItem(
                appId = 1,
                appName = "App1",
                packageName = null,
                urlPattern = "https://example.com/1",
                versionName = "1.0",
                state = DownloadState.Idle,
                order = 0
            )
        )

        val state = BulkDownloadState(
            items = items,
            isActive = true,
            completedCount = 5,
            failedCount = 1,
            totalCount = 6,
            completedAt = 12345678L
        )

        assertEquals(1, state.items.size)
        assertTrue(state.isActive)
        assertEquals(5, state.completedCount)
        assertEquals(1, state.failedCount)
        assertEquals(6, state.totalCount)
        assertEquals(12345678L, state.completedAt)
    }
}
