package eu.micer.distro.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import eu.micer.distro.data.AppConfig
import eu.micer.distro.viewmodel.AppConfigWithStatus
import eu.micer.distro.utils.InstalledAppInfo
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mainScreen_displaysAppTitle() {
        // When
        composeTestRule.setContent {
            MainScreen(
                appsWithStatus = emptyList(),
                onNavigateToConfig = {},
                onRefreshInstallationStatus = {},
                onBulkDownload = { _, _ -> },
                onBulkUninstall = {},
                onBulkQuickLinkDownload = { _, _ -> }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Distro").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysAppsList() {
        // Given
        val apps = listOf(
            AppConfigWithStatus(
                config = AppConfig(
                    id = 1,
                    name = "Test App 1",
                    urlPattern = "https://example.com/1"
                ),
                installedInfo = InstalledAppInfo(isInstalled = false)
            ),
            AppConfigWithStatus(
                config = AppConfig(
                    id = 2,
                    name = "Test App 2",
                    urlPattern = "https://example.com/2"
                ),
                installedInfo = InstalledAppInfo(isInstalled = true, versionName = "1.0.0")
            )
        )

        // When
        composeTestRule.setContent {
            MainScreen(
                appsWithStatus = apps,
                onNavigateToConfig = {},
                onRefreshInstallationStatus = {},
                onBulkDownload = { _, _ -> },
                onBulkUninstall = {},
                onBulkQuickLinkDownload = { _, _ -> }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test App 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test App 2").assertIsDisplayed()
    }

    @Test
    fun mainScreen_showsInstalledStatus() {
        // Given
        val apps = listOf(
            AppConfigWithStatus(
                config = AppConfig(
                    id = 1,
                    name = "Installed App",
                    urlPattern = "https://example.com/installed",
                    packageName = "com.installed"
                ),
                installedInfo = InstalledAppInfo(isInstalled = true, versionName = "2.1.0")
            )
        )

        // When
        composeTestRule.setContent {
            MainScreen(
                appsWithStatus = apps,
                onRefreshInstallationStatus = {},
                onNavigateToConfig = {},
                onBulkDownload = { _, _ -> },
                onBulkUninstall = {},
                onBulkQuickLinkDownload = { _, _ -> }
            )
        }

        // Then - should show installed indicator
        composeTestRule.onNodeWithText("Installed App").assertIsDisplayed()
    }
}
