package eu.micer.distro.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DownloadStateTest {

    @Test
    fun `DownloadState Idle is singleton`() {
        assertTrue(DownloadState.Idle === DownloadState.Idle)
    }

    @Test
    fun `DownloadState Downloading with determinate progress`() {
        // Given
        val state = DownloadState.Downloading(
            progress = 0.5f,
            isIndeterminate = false,
            downloadedBytes = 1024L
        )

        // Then
        assertEquals(0.5f, state.progress, 0.001f)
        assertFalse(state.isIndeterminate)
        assertEquals(1024L, state.downloadedBytes)
    }

    @Test
    fun `DownloadState Downloading with indeterminate progress`() {
        // Given
        val state = DownloadState.Downloading(
            progress = 0f,
            isIndeterminate = true,
            downloadedBytes = 2048L
        )

        // Then
        assertEquals(0f, state.progress, 0.001f)
        assertTrue(state.isIndeterminate)
        assertEquals(2048L, state.downloadedBytes)
    }

    @Test
    fun `DownloadState Downloading default values`() {
        // Given
        val state = DownloadState.Downloading(progress = 0.75f)

        // Then - defaults should be isIndeterminate = false, downloadedBytes = 0
        assertEquals(0.75f, state.progress, 0.001f)
        assertFalse(state.isIndeterminate)
        assertEquals(0L, state.downloadedBytes)
    }

    @Test
    fun `DownloadState Downloading copy function`() {
        // Given
        val original = DownloadState.Downloading(
            progress = 0.3f,
            isIndeterminate = false,
            downloadedBytes = 500L
        )

        // When
        val copy = original.copy(progress = 0.8f)

        // Then
        assertEquals(0.8f, copy.progress, 0.001f)
        assertFalse(copy.isIndeterminate) // Unchanged
        assertEquals(500L, copy.downloadedBytes) // Unchanged
    }

    @Test
    fun `DownloadState Success holds file and metadata`() {
        // Given
        val mockFile = File("/test/path/app.apk")
        val metadata = ApkMetadata(
            packageName = "com.test.app",
            versionName = "1.0.0",
            versionCode = 100L,
            appLabel = "Test App"
        )

        // When
        val state = DownloadState.Success(file = mockFile, metadata = metadata)

        // Then
        assertEquals(mockFile, state.file)
        assertEquals(metadata, state.metadata)
    }

    @Test
    fun `DownloadState Success with null metadata`() {
        // Given
        val mockFile = File("/test/path/app.apk")

        // When
        val state = DownloadState.Success(file = mockFile, metadata = null)

        // Then
        assertEquals(mockFile, state.file)
        assertNull(state.metadata)
    }

    @Test
    fun `DownloadState Error holds message`() {
        // Given
        val errorMessage = "Network connection failed"

        // When
        val state = DownloadState.Error(message = errorMessage)

        // Then
        assertEquals(errorMessage, state.message)
    }

    @Test
    fun `DownloadState Error with empty message`() {
        // Given & When
        val state = DownloadState.Error(message = "")

        // Then
        assertEquals("", state.message)
    }

    @Test
    fun `DownloadState types are sealed and can be used in when expressions`() {
        // Verify all types exist and can be created
        val idle: DownloadState = DownloadState.Idle
        val downloading = DownloadState.Downloading(0.5f)
        val success = DownloadState.Success(File("test.apk"), null)
        val error = DownloadState.Error("error")

        // All should be valid DownloadState instances
        assertTrue(idle is DownloadState)

        // Can be used in when expressions
        val result = when (downloading) {
            is DownloadState.Idle -> "idle"
            is DownloadState.Downloading -> "downloading"
            is DownloadState.Success -> "success"
            is DownloadState.Error -> "error"
        }
        assertEquals("downloading", result)
    }

    @Test
    fun `DownloadState Downloading progress at zero`() {
        val state = DownloadState.Downloading(progress = 0f)
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun `DownloadState Downloading progress at one hundred percent`() {
        val state = DownloadState.Downloading(progress = 1.0f)
        assertEquals(1.0f, state.progress, 0.001f)
    }

    @Test
    fun `DownloadState Downloading with large byte count`() {
        val largeBytes = 1024L * 1024L * 1024L // 1GB
        val state = DownloadState.Downloading(
            progress = 0.5f,
            downloadedBytes = largeBytes
        )
        assertEquals(largeBytes, state.downloadedBytes)
    }
}
