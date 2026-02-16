package eu.micer.distro.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var appDao: AppDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        appDao = db.appDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadApp() = runBlocking {
        // Given
        val app = AppConfig(
            name = "Test App",
            urlPattern = "https://example.com/app-{version}.apk",
            packageName = "com.example.test",
            versionName = "1.0.0",
            versionCode = 100,
            appLabel = "Test Application",
            quickLinks = """[{"name":"Production","link":"https://example.com/prod.apk"}]"""
        )

        // When
        val id = appDao.insertApp(app)
        val retrieved = appDao.getAppById(id)

        // Then
        assertNotNull(retrieved)
        assertEquals("Test App", retrieved?.name)
        assertEquals("https://example.com/app-{version}.apk", retrieved?.urlPattern)
        assertEquals("com.example.test", retrieved?.packageName)
        assertEquals("1.0.0", retrieved?.versionName)
        assertEquals(100L, retrieved?.versionCode)
        assertEquals("Test Application", retrieved?.appLabel)
    }

    @Test
    fun insertAppsAndReadAll() = runBlocking {
        // Given
        val apps = listOf(
            AppConfig(name = "App1", urlPattern = "https://example.com/1"),
            AppConfig(name = "App2", urlPattern = "https://example.com/2"),
            AppConfig(name = "App3", urlPattern = "https://example.com/3")
        )

        // When
        appDao.insertApps(apps)
        val allApps = appDao.getAllApps().first()

        // Then
        assertEquals(3, allApps.size)
        assertTrue(allApps.any { it.name == "App1" })
        assertTrue(allApps.any { it.name == "App2" })
        assertTrue(allApps.any { it.name == "App3" })
    }

    @Test
    fun getAppByIdReturnsNullForNonExistent() = runBlocking {
        // When
        val result = appDao.getAppById(9999)

        // Then
        assertNull(result)
    }

    @Test
    fun updateApp() = runBlocking {
        // Given
        val app = AppConfig(name = "Original", urlPattern = "https://example.com/original")
        val id = appDao.insertApp(app)
        val inserted = appDao.getAppById(id)!!

        // When
        val updated = inserted.copy(name = "Updated", urlPattern = "https://example.com/updated")
        appDao.updateApp(updated)
        val retrieved = appDao.getAppById(id)

        // Then
        assertEquals("Updated", retrieved?.name)
        assertEquals("https://example.com/updated", retrieved?.urlPattern)
    }

    @Test
    fun deleteApp() = runBlocking {
        // Given
        val app = AppConfig(name = "ToDelete", urlPattern = "https://example.com/delete")
        val id = appDao.insertApp(app)
        val inserted = appDao.getAppById(id)!!

        // When
        appDao.deleteApp(inserted)
        val retrieved = appDao.getAppById(id)

        // Then
        assertNull(retrieved)
    }

    @Test
    fun allAppsReturnsEmptyListInitially() = runBlocking {
        // When
        val apps = appDao.getAllApps().first()

        // Then
        assertTrue(apps.isEmpty())
    }

    @Test
    fun urlPatternUniqueConstraint() = runBlocking {
        // Given
        val app1 = AppConfig(name = "App1", urlPattern = "https://example.com/unique")
        appDao.insertApp(app1)

        // When - insert another app with same urlPattern
        val app2 = AppConfig(name = "App2", urlPattern = "https://example.com/unique")
        val id2 = appDao.insertApp(app2)

        // Then - due to OnConflictStrategy.REPLACE, app2 should replace app1
        val allApps = appDao.getAllApps().first()
        assertEquals(1, allApps.size)
        assertEquals("App2", allApps[0].name)
    }

    @Test
    fun appsAreSortedByName() = runBlocking {
        // Given
        val apps = listOf(
            AppConfig(name = "Zebra", urlPattern = "https://example.com/z"),
            AppConfig(name = "Apple", urlPattern = "https://example.com/a"),
            AppConfig(name = "Mango", urlPattern = "https://example.com/m")
        )

        // When
        appDao.insertApps(apps)
        val allApps = appDao.getAllApps().first()

        // Then
        assertEquals("Apple", allApps[0].name)
        assertEquals("Mango", allApps[1].name)
        assertEquals("Zebra", allApps[2].name)
    }

    @Test
    fun insertWithNullFields() = runBlocking {
        // Given
        val app = AppConfig(
            name = "Minimal",
            urlPattern = "https://example.com/minimal",
            packageName = null,
            versionName = null,
            versionCode = null,
            appLabel = null,
            quickLinks = null
        )

        // When
        val id = appDao.insertApp(app)
        val retrieved = appDao.getAppById(id)

        // Then
        assertNotNull(retrieved)
        assertEquals("Minimal", retrieved?.name)
        assertNull(retrieved?.packageName)
        assertNull(retrieved?.versionName)
    }

    @Test
    fun autoGeneratedIdIncrements() = runBlocking {
        // Given
        val app1 = AppConfig(name = "First", urlPattern = "https://example.com/1")
        val app2 = AppConfig(name = "Second", urlPattern = "https://example.com/2")

        // When
        val id1 = appDao.insertApp(app1)
        val id2 = appDao.insertApp(app2)

        // Then
        assertTrue(id2 > id1)
    }

    @Test
    fun updateAllFields() = runBlocking {
        // Given
        val app = AppConfig(name = "Original", urlPattern = "https://example.com/orig")
        val id = appDao.insertApp(app)
        val inserted = appDao.getAppById(id)!!

        // When
        val updated = inserted.copy(
            name = "All Updated",
            urlPattern = "https://example.com/all-updated",
            packageName = "com.updated",
            versionName = "2.0.0",
            versionCode = 200,
            appLabel = "Updated App",
            quickLinks = """[{"name":"New","link":"https://new.com"}]"""
        )
        appDao.updateApp(updated)
        val retrieved = appDao.getAppById(id)

        // Then
        assertEquals("All Updated", retrieved?.name)
        assertEquals("https://example.com/all-updated", retrieved?.urlPattern)
        assertEquals("com.updated", retrieved?.packageName)
        assertEquals("2.0.0", retrieved?.versionName)
        assertEquals(200L, retrieved?.versionCode)
        assertEquals("Updated App", retrieved?.appLabel)
    }

    @Test
    fun flowEmitsUpdates() = runBlocking {
        // Given - initial empty list
        val flow = appDao.getAllApps()
        var emissionCount = 0
        var lastEmission: List<AppConfig>? = null

        // Collect first emission
        val first = flow.first()
        assertEquals(0, first.size)

        // When - insert an app
        appDao.insertApp(AppConfig(name = "New", urlPattern = "https://example.com/new"))

        // Then - new emission should have the app
        val second = flow.first()
        assertEquals(1, second.size)
        assertEquals("New", second[0].name)
    }
}
