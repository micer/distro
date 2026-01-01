package eu.micer.distro.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import timber.log.Timber
import java.io.File

class ApkInstaller(private val context: Context) {
    
    fun installApk(file: File): Result<Unit> {
        return try {
            Timber.d("Installing APK: ${file.name} (${file.length() / 1024}KB)")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                installFromUri(uri)
            } else {
                val uri = Uri.fromFile(file)
                installFromUri(uri)
            }
            Timber.i("Installation intent launched successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to install APK: ${file.name}")
            Result.failure(e)
        }
    }
    
    private fun installFromUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    fun uninstallApp(packageName: String): Result<Unit> {
        return try {
            Timber.d("Uninstalling app: $packageName")
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = "package:$packageName".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Timber.i("Uninstallation intent launched successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to uninstall app: $packageName")
            Result.failure(e)
        }
    }
}
