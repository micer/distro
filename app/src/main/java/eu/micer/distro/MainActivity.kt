package eu.micer.distro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import eu.micer.distro.ui.AddEditAppScreen
import eu.micer.distro.ui.AppConfigScreen
import eu.micer.distro.ui.MainScreen
import eu.micer.distro.ui.theme.DistroTheme
import eu.micer.distro.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DistroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DistroAppContent()
                }
            }
        }
    }
}

@Composable
fun DistroAppContent() {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel()
    val appsWithStatus by viewModel.appsWithStatus.collectAsState()
val importState by viewModel.importState.collectAsState()
val bulkDownloadState by viewModel.bulkDownloadState.collectAsState()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                appsWithStatus = appsWithStatus,
                onNavigateToConfig = { navController.navigate("config") },
                onRefreshInstallationStatus = { viewModel.refreshInstallationStatus() },
                onBulkDownload = { ids: List<Long>, version: String -> viewModel.bulkDownloadAndInstall(ids, version) },
                onBulkUninstall = { ids: List<Long> -> viewModel.bulkUninstall(ids) },
                onBulkQuickLinkDownload = { ids, name -> viewModel.bulkDownloadFromQuickLinkName(ids, name) },
                bulkDownloadState = bulkDownloadState,
                onCancelBulkDownload = { viewModel.cancelBulkDownload() }
            )
        }
        
        composable("add_app") {
            AddEditAppScreen(
                onNavigateBack = { navController.popBackStack() },
                onSave = { name, pattern, packageName, quickLinksJson ->
                    viewModel.insertApp(name, pattern, packageName, quickLinksJson)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "edit_app/{appId}",
            arguments = listOf(navArgument("appId") { type = NavType.LongType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getLong("appId") ?: 0L
            var appName by remember { mutableStateOf<String?>(null) }
            var urlPattern by remember { mutableStateOf("") }
            var packageName by remember { mutableStateOf("") }
            var quickLinksJson by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(appId) {
                viewModel.getAppById(appId) { app ->
                    if (app != null) {
                        appName = app.name
                        urlPattern = app.urlPattern
                        packageName = app.packageName ?: ""
                        quickLinksJson = app.quickLinks
                    }
                }
            }

            appName?.let { name ->
                AddEditAppScreen(
                    appName = name,
                    urlPattern = urlPattern,
                    packageName = packageName,
                    quickLinksJson = quickLinksJson,
                    onNavigateBack = { navController.popBackStack() },
                    onSave = { updatedName, pattern, pkg, quickLinks ->
                        viewModel.updateApp(appId, updatedName, pattern, pkg, quickLinks)
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable("config") {
            AppConfigScreen(
                apps = appsWithStatus.map { it.config },
                importState = importState,
                onNavigateBack = { navController.popBackStack() },
                onAddApp = { navController.navigate("add_app") },
                onEditApp = { appId -> 
                    navController.navigate("edit_app/$appId")
                },
                onDeleteApp = { app -> viewModel.deleteApp(app) },
                onExportApps = { viewModel.exportAppsToJson() },
                onImport = { json -> viewModel.importAppsFromJson(json) },
                onResetImportState = { viewModel.resetImportState() }
            )
        }
    }
}
