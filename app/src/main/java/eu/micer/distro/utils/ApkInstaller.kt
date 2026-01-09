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
            Timber.d("=== UNINSTALL DEBUG START ===")
            Timber.d("Package name: '$packageName'")
            Timber.d("Context type: ${context.javaClass.name}")
            Timber.d("Context package: ${context.packageName}")
            
            // Build the URI
            val uriString = "package:$packageName"
            Timber.d("URI string: '$uriString'")
            val uri = Uri.parse(uriString)
            Timber.d("Parsed URI: scheme='${uri.scheme}', schemeSpecificPart='${uri.schemeSpecificPart}', full='$uri'")
            
            // Build the intent
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = uri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            Timber.d("Intent action: '${intent.action}'")
            Timber.d("Intent data: '${intent.data}'")
            Timber.d("Intent flags: ${intent.flags} (NEW_TASK flag value: ${Intent.FLAG_ACTIVITY_NEW_TASK})")
            Timber.d("Intent component: ${intent.component}")
            Timber.d("Intent package: ${intent.`package`}")
            
            // CRITICAL: Check if intent can be resolved by the system
            val pm = context.packageManager
            val resolveInfo = pm.resolveActivity(intent, 0)
            if (resolveInfo != null) {
                Timber.d("✓ Intent CAN be resolved")
                Timber.d("  Resolver: ${resolveInfo.activityInfo.packageName}")
                Timber.d("  Activity: ${resolveInfo.activityInfo.name}")
                Timber.d("  Enabled: ${resolveInfo.activityInfo.enabled}")
                Timber.d("  Exported: ${resolveInfo.activityInfo.exported}")
            } else {
                Timber.e("✗ Intent CANNOT be resolved - NO activity can handle ACTION_DELETE!")
                Timber.e("This means the system has no uninstaller activity registered")
                return Result.failure(Exception("No activity found to handle ACTION_DELETE"))
            }
            
            Timber.d("Calling context.startActivity()...")
            context.startActivity(intent)
            Timber.d("startActivity() returned without exception")
            Timber.d("=== UNINSTALL DEBUG END ===")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Exception during uninstall!")
            Timber.e("Exception type: ${e.javaClass.name}")
            Timber.e("Exception message: ${e.message}")
            Timber.e("Stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
}
