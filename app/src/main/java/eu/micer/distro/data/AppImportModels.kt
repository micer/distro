package eu.micer.distro.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class QuickLink(
    val name: String,
    val link: String
)

@Serializable
data class AppImportList(
    val version: String,
    val apps: List<AppImportItem>
)

@Serializable
data class AppImportItem(
    val name: String,
    val urlPattern: String,
    val packageName: String? = null,
    val quickLinks: List<QuickLink> = emptyList()
)

fun AppImportItem.toAppConfig(): AppConfig {
    return AppConfig(
        name = name,
        urlPattern = urlPattern,
        packageName = packageName?.trim()?.ifBlank { null },
        quickLinks = quickLinksToJson(quickLinks)
    )
}

fun AppConfig.toAppImportItem(): AppImportItem {
    return AppImportItem(
        name = name,
        urlPattern = urlPattern,
        packageName = packageName,
        quickLinks = quickLinksFromJson(quickLinks)
    )
}

fun quickLinksToJson(quickLinks: List<QuickLink>): String {
    if (quickLinks.isEmpty()) return ""
    return Json.encodeToString(quickLinks)
}

fun quickLinksFromJson(jsonString: String?): List<QuickLink> {
    if (jsonString.isNullOrBlank()) return emptyList()
    return try {
        Json.decodeFromString(jsonString)
    } catch (e: Exception) {
        emptyList()
    }
}
