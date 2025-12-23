package eu.micer.distro.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import timber.log.Timber

data class InstalledAppInfo(
    val isInstalled: Boolean,
    val versionName: String? = null,
    val versionCode: Long? = null
)

class InstalledAppChecker(private val context: Context) {
    
    /**
     * Checks if an app with the given package name is installed
     * and returns its version information
     * @param packageName The package name to check
     * @return InstalledAppInfo with installation status and version info
     */
    fun getInstalledAppInfo(packageName: String?): InstalledAppInfo {
        if (packageName.isNullOrBlank()) {
            return InstalledAppInfo(isInstalled = false)
        }
        
        return try {
            val packageManager = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            
            val info = InstalledAppInfo(
                isInstalled = true,
                versionName = packageInfo.versionName,
                versionCode = getVersionCode(packageInfo)
            )
            Timber.d("App installed: $packageName v${info.versionName}")
            info
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.d("App not installed or not visible (check QUERY_ALL_PACKAGES permission): $packageName")
            InstalledAppInfo(isInstalled = false)
        } catch (e: Exception) {
            Timber.e(e, "Error checking installation status for: $packageName")
            InstalledAppInfo(isInstalled = false)
        }
    }
    
    private fun getVersionCode(packageInfo: PackageInfo): Long {
        return packageInfo.longVersionCode
    }
}
