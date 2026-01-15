package eu.micer.distro.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import timber.log.Timber
import androidx.lifecycle.viewModelScope
import eu.micer.distro.data.AppConfig
import eu.micer.distro.data.AppDatabase
import eu.micer.distro.data.AppImportList
import eu.micer.distro.data.AppRepository
import eu.micer.distro.data.toAppConfig
import eu.micer.distro.data.toAppImportItem
import eu.micer.distro.utils.ApkInstaller
import eu.micer.distro.utils.DownloadManager
import eu.micer.distro.utils.DownloadState
import eu.micer.distro.utils.InstalledAppChecker
import eu.micer.distro.utils.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

data class AppConfigWithStatus(
    val config: AppConfig,
    val installedInfo: InstalledAppInfo
)

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Success(val count: Int) : ImportState()
    data class Error(val message: String) : ImportState()
}

// Represents the download state for a single app in a bulk operation
data class BulkDownloadItem(
    val appId: Long,
    val appName: String,
    val packageName: String?,
    val urlPattern: String,
    val versionName: String,
    val state: DownloadState,
    val order: Int // Position in the queue
)

// Overall state of the bulk download operation
data class BulkDownloadState(
    val items: List<BulkDownloadItem> = emptyList(),
    val isActive: Boolean = false,
    val completedCount: Int = 0,
    val failedCount: Int = 0,
    val totalCount: Int = 0
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AppRepository
    private val downloadManager: DownloadManager
    private val apkInstaller: ApkInstaller
    private val installedAppChecker: InstalledAppChecker
    
    val allApps: StateFlow<List<AppConfig>>
    
    // Combined flow with installation status
    val appsWithStatus: StateFlow<List<AppConfigWithStatus>>
    
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
val importState: StateFlow<ImportState> = _importState.asStateFlow()

private val _bulkDownloadState = MutableStateFlow(BulkDownloadState())
val bulkDownloadState: StateFlow<BulkDownloadState> = _bulkDownloadState.asStateFlow()

private val uninstallQueue = mutableListOf<Long>()
private var bulkDownloadJob: Job? = null
    
    // Trigger to refresh installation status
    private val _refreshTrigger = MutableStateFlow(0)
    
    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
        downloadManager = DownloadManager(application)
        apkInstaller = ApkInstaller(application)
        installedAppChecker = InstalledAppChecker(application)
        
        allApps = repository.allApps
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
        // Map apps to include installation status
        // Combine with refresh trigger to re-check installation status when needed
        appsWithStatus = combine(allApps, _refreshTrigger) { apps, _ ->
            apps.map { app ->
                Timber.d("Checking installation status for ${app.packageName}")
                val installedInfo = installedAppChecker.getInstalledAppInfo(app.packageName)
                AppConfigWithStatus(app, installedInfo)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    fun insertApp(name: String, urlPattern: String, packageName: String = "") {
        viewModelScope.launch {
            val finalPackageName = packageName.trim().ifBlank { null }
            repository.insertApp(AppConfig(name = name, urlPattern = urlPattern, packageName = finalPackageName))
        }
    }
    
    fun updateApp(app: AppConfig) {
        viewModelScope.launch {
            repository.updateApp(app)
        }
    }
    
    fun updateApp(appId: Long, name: String, urlPattern: String, packageName: String = "") {
        viewModelScope.launch {
            getAppById(appId) { existingApp ->
                existingApp?.let { app ->
                    val finalPackageName = packageName.trim().ifBlank { null }
                    val updatedApp = app.copy(
                        name = name,
                        urlPattern = urlPattern,
                        packageName = finalPackageName
                    )
                    launch {
                        repository.updateApp(updatedApp)
                    }
                }
            }
        }
    }
    
    fun deleteApp(app: AppConfig) {
        viewModelScope.launch {
            repository.deleteApp(app)
        }
    }
    
    fun importAppsFromJson(jsonString: String) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                // Parse JSON on Default dispatcher
                val importList = withContext(Dispatchers.Default) {
                    Json.decodeFromString<AppImportList>(jsonString)
                }
                
                // Map to AppConfig
                val appsToInsert = importList.apps.map { it.toAppConfig() }
                
                // Insert apps using repository
                repository.insertApps(appsToInsert)
                
                // Update state
                _importState.value = ImportState.Success(appsToInsert.size)
            } catch (e: Exception) {
                Timber.e(e, "Failed to import apps from JSON")
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun exportAppsToJson(): String {
//        val apps = allApps.value
//        val exportList = AppImportList(
//            version = "1.0",
//            apps = apps.map { it.toAppImportItem() }
//        )
//        return Json.encodeToString(AppImportList.serializer(), exportList)
        Toast.makeText(getApplication(), "Not implemented yet", Toast.LENGTH_SHORT).show()
        return ""
    }

    fun resetImportState() {
        _importState.value = ImportState.Idle
    }
    
    fun getAppById(id: Long, callback: (AppConfig?) -> Unit) {
        viewModelScope.launch {
            val app = repository.getAppById(id)
            callback(app)
        }
    }

    fun bulkDownloadAndInstall(appIds: List<Long>, versionName: String) {
        // Cancel any existing bulk download
        bulkDownloadJob?.cancel()
        
        bulkDownloadJob = viewModelScope.launch {
            // Prepare the download list
            val downloadItems = appIds.mapIndexedNotNull { index, appId ->
                val app = repository.getAppById(appId)
                app?.let {
                    BulkDownloadItem(
                        appId = appId,
                        appName = app.name.ifBlank { app.appLabel ?: "App" },
                        packageName = app.packageName,
                        urlPattern = app.urlPattern,
                        versionName = versionName,
                        state = DownloadState.Idle,
                        order = index
                    )
                }
            }
            
            // Initialize bulk download state
            _bulkDownloadState.value = BulkDownloadState(
                items = downloadItems,
                isActive = true,
                completedCount = 0,
                failedCount = 0,
                totalCount = downloadItems.size
            )
            
            // Semaphore to limit concurrent downloads to 3
            val semaphore = Semaphore(10)
            
            // Launch parallel downloads
            val jobs = downloadItems.map { item ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        downloadSingleAppInBulk(item)
                    }
                }
            }
            
            // Wait for all downloads to complete
            jobs.awaitAll()
            
            // Mark bulk download as complete
            _bulkDownloadState.update { state ->
                state.copy(isActive = false)
            }
        }
    }

    private suspend fun downloadSingleAppInBulk(item: BulkDownloadItem) {
        try {
            val url = item.urlPattern.replace("{version}", item.versionName)
            
            withContext(Dispatchers.IO) {
                downloadManager.downloadApk(url).collect { state ->
                    // Update this specific item's state
                    _bulkDownloadState.update { bulkState ->
                        val updatedItems = bulkState.items.map { 
                            if (it.appId == item.appId) it.copy(state = state) else it 
                        }
                        bulkState.copy(items = updatedItems)
                    }
                    
                    when (state) {
                        is DownloadState.Success -> {
                            // Update app config with APK metadata
                            state.metadata?.let { metadata ->
                                val app = repository.getAppById(item.appId)
                                app?.let {
                                    val updatedApp = it.copy(
                                        name = if (it.name.isBlank() || it.name == "App") metadata.appLabel else it.name,
                                        packageName = metadata.packageName,
                                        versionName = metadata.versionName,
                                        versionCode = metadata.versionCode,
                                        appLabel = metadata.appLabel
                                    )
                                    repository.updateApp(updatedApp)
                                }
                            }

                            // Trigger installation
                            withContext(Dispatchers.Main) {
                                apkInstaller.installApk(state.file)
                            }
                            // Note: We don't delete the APK immediately because the system installer
                            // still needs to access it via FileProvider. The file will be cleaned up
                            // when the app resumes/refreshes instead.

                            // Update completed count
                            _bulkDownloadState.update { bulkState ->
                                bulkState.copy(completedCount = bulkState.completedCount + 1)
                            }
                        }
                        is DownloadState.Error -> {
                            Timber.e("Download error for ${item.appName}: ${state.message}")
                            
                            // Update failed count
                            _bulkDownloadState.update { bulkState ->
                                bulkState.copy(
                                    completedCount = bulkState.completedCount + 1,
                                    failedCount = bulkState.failedCount + 1
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during bulk download for ${item.appName}")
            
            // Update item state to error
            _bulkDownloadState.update { bulkState ->
                val updatedItems = bulkState.items.map { 
                    if (it.appId == item.appId) 
                        it.copy(state = DownloadState.Error(e.message ?: "Unknown error")) 
                    else it 
                }
                bulkState.copy(
                    items = updatedItems,
                    completedCount = bulkState.completedCount + 1,
                    failedCount = bulkState.failedCount + 1
                )
            }
        }
    }

    fun bulkUninstall(appIds: List<Long>) {
        viewModelScope.launch {
            uninstallQueue.clear()
            var skippedCount = 0
            // Only add apps that have a package name
            for (appId in appIds) {
                val app = repository.getAppById(appId)
                if (app?.packageName != null) {
                    Timber.d("Adding ${app.packageName} to uninstall queue")
                    uninstallQueue.add(appId)
                } else {
                    skippedCount++
                    Timber.w("App $appId (${app?.name}) has no package name, cannot uninstall")
                }
            }
            Timber.d("Uninstall queue size: ${uninstallQueue.size}, skipped: $skippedCount")
            
            if (skippedCount > 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Skipped $skippedCount app(s) without package name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            if (uninstallQueue.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "No apps to uninstall (missing package names)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                processNextUninstall()
            }
        }
    }

    private fun processNextUninstall() {
        Timber.d("processNextUninstall called, queue size: ${uninstallQueue.size}")
        if (uninstallQueue.isNotEmpty()) {
            val appId = uninstallQueue.removeAt(0)
            val remainingCount = uninstallQueue.size
            viewModelScope.launch {
                val app = repository.getAppById(appId)
                app?.packageName?.let { pkg ->
                    Timber.d("Uninstalling package: $pkg (${remainingCount} remaining)")
                    apkInstaller.uninstallApp(pkg)
                } ?: run {
                    Timber.w("App $appId has no package name, skipping")
                    processNextUninstall()
                }
            }
        } else {
            Timber.d("Uninstall queue is empty - all uninstalls completed")
        }
    }
    
    fun cancelBulkDownload() {
        bulkDownloadJob?.cancel()
        _bulkDownloadState.value = BulkDownloadState()

        // Clean up any partial APK files from cache immediately
        cleanupAllApkFiles()
    }

    /**
     * Refreshes the installation status of all apps
     * Call this when returning to the main screen to check if any apps were installed
     */
    fun refreshInstallationStatus() {
        _refreshTrigger.value += 1
        if (uninstallQueue.isNotEmpty()) {
            processNextUninstall()
        }
        // Clean up old APK files when returning to main screen
        // (by this time, installations should be complete or cancelled)
        cleanupOldApkFiles()
    }

    /**
     * Clean up old APK files from the cache directory
     * This should be called when the app resumes to free up disk space
     */
    private fun cleanupOldApkFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheDir = getApplication<Application>().cacheDir
                val apkFiles = cacheDir.listFiles { file ->
                    file.name.startsWith("temp_") && file.extension == "apk"
                }
                apkFiles?.forEach { file ->
                    val ageInMillis = System.currentTimeMillis() - file.lastModified()
                    val ageInSeconds = ageInMillis / 1000

                    // Delete APKs older than 10 seconds to ensure system installer is done
                    // 10 seconds gives enough time for the installation dialog to appear
                    if (ageInSeconds > 10) {
                        val deleted = file.delete()
                        if (deleted) {
                            Timber.d("Cleaned up old APK file: ${file.name} (${ageInSeconds}s old)")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error cleaning up old APK files")
            }
        }
    }

    /**
     * Clean up ALL APK files from cache (immediate cleanup for cancel operations)
     */
    private fun cleanupAllApkFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheDir = getApplication<Application>().cacheDir
                val apkFiles = cacheDir.listFiles { file ->
                    file.name.startsWith("temp_") && file.extension == "apk"
                }
                apkFiles?.forEach { file ->
                    val deleted = file.delete()
                    if (deleted) {
                        Timber.d("Cleaned up APK file: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error cleaning up APK files")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        downloadManager.close()
    }
}
