package eu.micer.distro.data

import kotlinx.serialization.Serializable

@Serializable
data class AppImportData(
    val name: String,
    val urlPattern: String,
    val packageName: String? = null
)

@Serializable
data class ImportFormat(
    val version: String,
    val apps: List<AppImportData>
)
