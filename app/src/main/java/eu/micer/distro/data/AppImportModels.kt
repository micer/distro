package eu.micer.distro.data

import kotlinx.serialization.Serializable

@Serializable
data class AppImportList(
    val version: String,
    val apps: List<AppImportItem>
)

@Serializable
data class AppImportItem(
    val name: String,
    val urlPattern: String,
    val packageName: String? = null
)

fun AppImportItem.toAppConfig(): AppConfig {
    return AppConfig(
        name = name,
        urlPattern = urlPattern,
        packageName = packageName
    )
}

fun AppConfig.toAppImportItem(): AppImportItem {
    return AppImportItem(
        name = name,
        urlPattern = urlPattern,
        packageName = packageName
    )
}
