package eu.micer.distro.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_configs",
    indices = [Index(value = ["urlPattern"], unique = true)]
)
data class AppConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // User-provided name or extracted from APK
    val urlPattern: String,
    val packageName: String? = null, // Extracted from APK (e.g., "com.example.app")
    val versionName: String? = null, // Extracted from APK (e.g., "1.2.3")
    val versionCode: Long? = null, // Extracted from APK
    val appLabel: String? = null, // Extracted app label from APK
    val quickLinks: String? = null // Serialized JSON list of QuickLink objects
)
