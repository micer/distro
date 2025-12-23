package eu.micer.distro.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import timber.log.Timber
import java.io.File

data class ApkMetadata(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val appLabel: String
)

class ApkParser(private val context: Context) {
    
    /**
     * Parses APK file and extracts metadata
     * @param apkFile The APK file to parse
     * @return ApkMetadata if successful, null otherwise
     */
    fun parseApk(apkFile: File): ApkMetadata? {
        return try {
            val packageManager = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            }
            
            if (packageInfo != null) {
                // Set application info for loading label
                packageInfo.applicationInfo?.sourceDir = apkFile.absolutePath
                packageInfo.applicationInfo?.publicSourceDir = apkFile.absolutePath
                
                val packageName = packageInfo.packageName
                val versionName = packageInfo.versionName ?: "Unknown"
                val versionCode = getVersionCode(packageInfo)
                val appLabel = packageInfo.applicationInfo?.let { appInfo ->
                    packageManager.getApplicationLabel(appInfo).toString()
                } ?: packageName
                
                Timber.d("Successfully parsed APK: $appLabel ($packageName) v$versionName")
                
                ApkMetadata(
                    packageName = packageName,
                    versionName = versionName,
                    versionCode = versionCode,
                    appLabel = appLabel
                )
            } else {
                Timber.w("Failed to parse APK: packageInfo is null for ${apkFile.name}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while parsing APK: ${apkFile.name}")
            null
        }
    }

    private fun getVersionCode(packageInfo: PackageInfo): Long {
        return packageInfo.longVersionCode
    }
}
