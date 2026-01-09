package eu.micer.distro.utils

import android.content.Context
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(
        val progress: Float, 
        val isIndeterminate: Boolean = false,
        val downloadedBytes: Long = 0L
    ) : DownloadState()
    data class Success(val file: File, val metadata: ApkMetadata?) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class DownloadManager(private val context: Context) {
    // Using OkHttp directly (bypassing Ktor to avoid 47-second delay bug)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val apkParser = ApkParser(context)
    
    suspend fun downloadApk(url: String): Flow<DownloadState> = flow {
        try {
            emit(DownloadState.Downloading(0f))
            
            val request = Request.Builder()
                .url(url)
                .build()
            
            client.newCall(request).execute().use { response ->
                Timber.d("Download started: ${response.code}")
                
                if (!response.isSuccessful) {
                    Timber.e("Download failed: HTTP ${response.code}")
                    emit(DownloadState.Error("Failed to download: ${response.code}"))
                    return@flow
                }
                
                val responseBody = response.body
                val contentLength = responseBody.contentLength()
                val isIndeterminate = contentLength <= 0L
                
                if (isIndeterminate) {
                    Timber.w("Content-Length is missing/zero - using indeterminate progress")
                }
                
                val fileName = "temp_${System.currentTimeMillis()}.apk"
                val outputFile = File(context.cacheDir, fileName)
                
                var downloadedBytes = 0L
                val buffer = ByteArray(8192)
                
                // Progress throttling: emit at most every 100ms or when progress increases by 1%
                val downloadStartTime = System.currentTimeMillis()
                var lastEmitTime = downloadStartTime
                var lastEmittedProgress = 0f
                val minProgressDelta = 0.01f
                val minTimeDelta = 100L
                
                responseBody.byteStream().use { inputStream ->
                    outputFile.outputStream().use { outputStream ->
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            
                            val currentTime = System.currentTimeMillis()
                            val timeSinceLastEmit = currentTime - lastEmitTime
                            
                            if (isIndeterminate) {
                                // For indeterminate progress, just emit periodically with downloaded bytes
                                if (timeSinceLastEmit >= minTimeDelta) {
                                    emit(DownloadState.Downloading(0f, true, downloadedBytes))
                                    lastEmitTime = currentTime
                                }
                            } else {
                                // For determinate progress, calculate percentage
                                val currentProgress = downloadedBytes.toFloat() / contentLength.toFloat()
                                val progressDelta = currentProgress - lastEmittedProgress
                                
                                if (timeSinceLastEmit >= minTimeDelta || progressDelta >= minProgressDelta) {
                                    emit(DownloadState.Downloading(currentProgress, false, downloadedBytes))
                                    lastEmitTime = currentTime
                                    lastEmittedProgress = currentProgress
                                }
                            }
                        }
                    }
                }
                
                val totalTime = System.currentTimeMillis() - downloadStartTime
                val avgSpeed = if (totalTime > 0) (downloadedBytes / 1024.0) / (totalTime / 1000.0) else 0.0
                val sizeInMB = downloadedBytes / (1024 * 1024)
                Timber.d("Download completed: ${sizeInMB}MB in ${totalTime}ms (${"%.1f".format(avgSpeed)} KB/s)")
                
                // Ensure we emit 100% for determinate progress
                if (!isIndeterminate && lastEmittedProgress < 1.0f) {
                    emit(DownloadState.Downloading(1.0f, false, downloadedBytes))
                }
                
                // Parse APK metadata
                val metadata = apkParser.parseApk(outputFile)
                if (metadata != null) {
                    Timber.d("APK parsed: ${metadata.appLabel} v${metadata.versionName}")
                }
                
                emit(DownloadState.Success(outputFile, metadata))
            }
        } catch (e: Exception) {
            Timber.e(e, "Download failed: ${e.message}")
            emit(DownloadState.Error(e.message ?: "Unknown error occurred"))
        }
    }.flowOn(Dispatchers.IO)
    
    fun close() {
        // OkHttpClient manages its own connection pool, no explicit close needed
    }
}
