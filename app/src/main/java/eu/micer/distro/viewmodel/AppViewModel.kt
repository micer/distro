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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

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

class AppViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AppRepository
    private val downloadManager: DownloadManager
    private val apkInstaller: ApkInstaller
    private val installedAppChecker: InstalledAppChecker
    
    val allApps: StateFlow<List<AppConfig>>
    
    // Combined flow with installation status
    val appsWithStatus: StateFlow<List<AppConfigWithStatus>>
    
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()
    
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()
    
    private var currentDownloadAppId: Long? = null
    
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
    
    fun downloadAndInstallApk(appId: Long, urlPattern: String, versionName: String) {
        currentDownloadAppId = appId
        val url = urlPattern.replace("{version}", versionName)
        
        viewModelScope.launch(Dispatchers.IO) {
            downloadManager.downloadApk(url).collect { state ->
                withContext(Dispatchers.Main) {
                    _downloadState.value = state
                }
                
                when (state) {
                    is DownloadState.Downloading -> {
                        // Progress updates happen automatically
                    }
                    is DownloadState.Success -> {
                        
                        // Update app config with APK metadata if available
                        state.metadata?.let { metadata ->
                            getAppById(appId) { existingApp ->
                                existingApp?.let { app ->
                                    val updatedApp = app.copy(
                                        name = if (app.name.isBlank() || app.name == "App") metadata.appLabel else app.name,
                                        packageName = metadata.packageName,
                                        versionName = metadata.versionName,
                                        versionCode = metadata.versionCode,
                                        appLabel = metadata.appLabel
                                    )
                                    updateApp(updatedApp)
                                }
                            }
                        }
                        
                        // Trigger installation (needs to be on Main thread for UI)
                        withContext(Dispatchers.Main) {
                            val result = apkInstaller.installApk(state.file)
                            if (result.isFailure) {
                                _downloadState.value = DownloadState.Error(
                                    result.exceptionOrNull()?.message ?: "Installation failed"
                                )
                            }
                        }
                    }
                    is DownloadState.Error -> {
                        Timber.e("Download error: ${state.message}")
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
        currentDownloadAppId = null
    }
    
    /**
     * Refreshes the installation status of all apps
     * Call this when returning to the main screen to check if any apps were installed
     */
    fun refreshInstallationStatus() {
        _refreshTrigger.value += 1
    }
    
    override fun onCleared() {
        super.onCleared()
        downloadManager.close()
    }
}
