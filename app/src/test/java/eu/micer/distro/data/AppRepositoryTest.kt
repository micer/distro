package eu.micer.distro.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppRepositoryTest {

    private lateinit var appDao: AppDao
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        appDao = mockk()
        // Must mock getAllApps() before creating repository because it's called in init
        every { appDao.getAllApps() } returns flowOf(emptyList())
        repository = AppRepository(appDao)
    }

    @Test
    fun `allApps should emit list from dao`() = runTest {
        // Given - create new repository with specific mock
        val apps = listOf(
            AppConfig(id = 1, name = "App1", urlPattern = "https://example.com/1"),
            AppConfig(id = 2, name = "App2", urlPattern = "https://example.com/2")
        )
        every { appDao.getAllApps() } returns flowOf(apps)
        val testRepository = AppRepository(appDao)

        // When
        val result = testRepository.allApps.first()

        // Then
        assertEquals(2, result.size)
        assertEquals("App1", result[0].name)
        assertEquals("App2", result[1].name)
    }

    @Test
    fun `getAppById should return app from dao`() = runTest {
        // Given
        val app = AppConfig(id = 1, name = "TestApp", urlPattern = "https://example.com/app")
        coEvery { appDao.getAppById(1) } returns app

        // When
        val result = repository.getAppById(1)

        // Then
        assertEquals("TestApp", result?.name)
        assertEquals("https://example.com/app", result?.urlPattern)
        coVerify(exactly = 1) { appDao.getAppById(1) }
    }

    @Test
    fun `getAppById should return null when app not found`() = runTest {
        // Given
        coEvery { appDao.getAppById(999) } returns null

        // When
        val result = repository.getAppById(999)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { appDao.getAppById(999) }
    }

    @Test
    fun `insertApp should delegate to dao and return id`() = runTest {
        // Given
        val app = AppConfig(name = "NewApp", urlPattern = "https://example.com/new")
        coEvery { appDao.insertApp(any()) } returns 42L

        // When
        val result = repository.insertApp(app)

        // Then
        assertEquals(42L, result)
        coVerify(exactly = 1) { appDao.insertApp(match { 
            it.name == "NewApp" && it.urlPattern == "https://example.com/new" 
        }) }
    }

    @Test
    fun `insertApp should preserve all app fields`() = runTest {
        // Given
        val app = AppConfig(
            name = "CompleteApp",
            urlPattern = "https://example.com/complete",
            packageName = "com.example.complete",
            versionName = "1.2.3",
            versionCode = 123,
            appLabel = "Complete App",
            quickLinks = """[{"name":"Link1","link":"https://link1.com"}]"""
        )
        coEvery { appDao.insertApp(any()) } returns 1L

        // When
        repository.insertApp(app)

        // Then
        coVerify { appDao.insertApp(match { 
            it.name == "CompleteApp" &&
            it.packageName == "com.example.complete" &&
            it.versionName == "1.2.3" &&
            it.versionCode == 123L &&
            it.appLabel == "Complete App"
        }) }
    }

    @Test
    fun `insertApps should delegate to dao with list`() = runTest {
        // Given
        val apps = listOf(
            AppConfig(name = "App1", urlPattern = "https://example.com/1"),
            AppConfig(name = "App2", urlPattern = "https://example.com/2")
        )
        coEvery { appDao.insertApps(any()) } returns Unit

        // When
        repository.insertApps(apps)

        // Then
        coVerify(exactly = 1) { appDao.insertApps(apps) }
    }

    @Test
    fun `insertApps should not throw with empty list`() = runTest {
        // Given
        val apps = emptyList<AppConfig>()
        coEvery { appDao.insertApps(any()) } returns Unit

        // When - should not throw
        repository.insertApps(apps)

        // Then - no exception means success
        assertTrue(true)
    }

    @Test
    fun `updateApp should delegate to dao`() = runTest {
        // Given
        val app = AppConfig(
            id = 1, 
            name = "UpdatedApp", 
            urlPattern = "https://example.com/updated",
            packageName = "com.updated"
        )
        coEvery { appDao.updateApp(any()) } returns Unit

        // When
        repository.updateApp(app)

        // Then
        coVerify(exactly = 1) { appDao.updateApp(app) }
    }

    @Test
    fun `deleteApp should delegate to dao`() = runTest {
        // Given
        val app = AppConfig(
            id = 1, 
            name = "DeletedApp", 
            urlPattern = "https://example.com/deleted"
        )
        coEvery { appDao.deleteApp(any()) } returns Unit

        // When
        repository.deleteApp(app)

        // Then
        coVerify(exactly = 1) { appDao.deleteApp(app) }
    }

    @Test
    fun `repository should use provided dao instance`() = runTest {
        // This test verifies the repository uses the DAO we provide
        val customDao = mockk<AppDao>()
        every { customDao.getAllApps() } returns flowOf(emptyList())
        
        // When
        val customRepo = AppRepository(customDao)
        customRepo.allApps.first()
        
        // Then - verify the custom dao was used
        coVerify { customDao.getAllApps() }
    }
}
