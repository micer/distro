package eu.micer.distro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import eu.micer.distro.ui.theme.Primary
import eu.micer.distro.ui.theme.Success
import eu.micer.distro.viewmodel.AppConfigWithStatus
import com.composables.icons.fontawesome.solid.R.drawable as fontAwesomeIcons

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    appsWithStatus: List<AppConfigWithStatus>,
    onNavigateToConfig: () -> Unit,
    onRefreshInstallationStatus: () -> Unit = {},
    onBulkDownload: (List<Long>, String) -> Unit = { _, _ -> },
    onBulkUninstall: (List<Long>) -> Unit = { _ -> },
    onBulkQuickLinkDownload: (List<Long>, String) -> Unit = { _, _ -> },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val isSelectionMode = selectedIds.isNotEmpty()

    var showVersionDialog by remember { mutableStateOf(false) }
    var showUninstallDialog by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    // Refresh installation status when the screen resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshInstallationStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = selectedIds.size,
                    onCloseSelection = { selectedIds = emptySet() },
                    onSelectAll = {
                        selectedIds = appsWithStatus.map { it.config.id }.toSet()
                    },
                    onDeselectAll = { selectedIds = emptySet() },
                    isAllSelected = selectedIds.size == appsWithStatus.size
                )
            } else {
                TopAppBar(
                    title = { Text("Distro - APK Distribution") },
                    actions = {
                        // New Settings/Config action
                        IconButton(onClick = { onNavigateToConfig() }) {
                            Icon(
                                painterResource(id = fontAwesomeIcons.fontawesome_ic_sliders_h_solid),
                                contentDescription = "App Configuration",
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Primary,
                        titleContentColor = androidx.compose.ui.graphics.Color.White,
                        actionIconContentColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        },
        floatingActionButton = {
            BulkActionsFab(
                isExpanded = isFabExpanded,
                onToggleExpand = { isFabExpanded = !isFabExpanded },
                onDownloadAll = {
                    showVersionDialog = true
                    isFabExpanded = false
                },
                onUninstallAll = {
                    showUninstallDialog = true
                    isFabExpanded = false
                },
                onEnterSelectionMode = {
                    if (selectedIds.isEmpty()) {
                        selectedIds = setOf(appsWithStatus.firstOrNull()?.config?.id ?: -1L).filter { it != -1L }.toSet()
                    }
                    isFabExpanded = false
                },
                isSelectionMode = isSelectionMode,
                onDownloadSelected = {
                    showVersionDialog = true
                },
                onUninstallSelected = {
                    showUninstallDialog = true
                }
            )
        }
    ) { padding ->
        if (appsWithStatus.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(appsWithStatus, key = { it.config.id }) { appWithStatus ->
                    val id = appWithStatus.config.id
                    AppItem(
                        appWithStatus = appWithStatus,
                        onClick = {
                    if (isSelectionMode) {
                        selectedIds = if (selectedIds.contains(id)) {
                            selectedIds - id
                        } else {
                            selectedIds + id
                        }
                    } else {
                        // For single app click, select it and show version dialog (uses bulk download)
                        selectedIds = setOf(id)
                        showVersionDialog = true
                    }
                },
                        onLongClick = {
                            if (!isSelectionMode) {
                                selectedIds = setOf(id)
                            }
                        },
                        selected = selectedIds.contains(id),
                        selectionMode = isSelectionMode
                    )
                }
            }
        }
    }

    if (showVersionDialog) {
        // Get the selected apps
        val selectedApps = if (isSelectionMode) {
            appsWithStatus.filter { selectedIds.contains(it.config.id) }.map { it.config }
        } else {
            appsWithStatus.map { it.config }
        }

        VersionInputDialog(
            selectedApps = selectedApps,
            onDismiss = { showVersionDialog = false },
            onConfirmByVersion = { version ->
                val idsToDownload = if (isSelectionMode) selectedIds.toList() else appsWithStatus.map { it.config.id }
                onBulkDownload(idsToDownload, version)
                showVersionDialog = false
                selectedIds = emptySet()
            },
            onQuickLinkClick = { quickLinkName ->
                onBulkQuickLinkDownload(selectedApps.map { it.id }, quickLinkName)
                showVersionDialog = false
                selectedIds = emptySet()
            }
        )
    }

    if (showUninstallDialog) {
        val idsToUninstall = if (isSelectionMode) {
            appsWithStatus
                .filter { selectedIds.contains(it.config.id) && it.installedInfo.isInstalled }
                .map { it.config.id }
        } else {
            appsWithStatus
                .filter { it.installedInfo.isInstalled }
                .map { it.config.id }
        }
        val count = idsToUninstall.size
        
        if (count == 0) {
            showUninstallDialog = false
        } else {
            AlertDialog(
                onDismissRequest = { showUninstallDialog = false },
                title = { Text("Bulk Uninstall") },
                text = { Text("Are you sure you want to uninstall $count app(s)?") },
                confirmButton = {
                    TextButton(onClick = {
                        onBulkUninstall(idsToUninstall)
                        showUninstallDialog = false
                        selectedIds = emptySet()
                    }) {
                        Text("Uninstall", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUninstallDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    selectedCount: Int,
    onCloseSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    isAllSelected: Boolean
) {
    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onCloseSelection) {
                Icon(Icons.Default.Close, contentDescription = "Close selection")
            }
        },
        actions = {
            IconButton(onClick = if (isAllSelected) onDeselectAll else onSelectAll) {
                Icon(
                    if (isAllSelected) Icons.Default.Close else Icons.Default.Check,
                    contentDescription = if (isAllSelected) "Deselect all" else "Select all"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
fun BulkActionsFab(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDownloadAll: () -> Unit,
    onUninstallAll: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    isSelectionMode: Boolean,
    onDownloadSelected: () -> Unit,
    onUninstallSelected: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isExpanded && !isSelectionMode) {
            FloatingActionButton(
                onClick = onDownloadAll,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download All")
            }
            FloatingActionButton(
                onClick = onUninstallAll,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Uninstall All")
            }
            FloatingActionButton(
                onClick = onEnterSelectionMode,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Select Apps")
            }
        }

        if (isSelectionMode) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(
                    onClick = onUninstallSelected,
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Uninstall Selected")
                }
                FloatingActionButton(
                    onClick = onDownloadSelected,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Download Selected")
                }
            }
        } else {
            FloatingActionButton(onClick = onToggleExpand) {
                Icon(
                    if (isExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = "Bulk Actions"
                )
            }
        }
    }
}

@Composable
fun VersionInputDialog(
    selectedApps: List<eu.micer.distro.data.AppConfig> = emptyList(),
    onDismiss: () -> Unit,
    onConfirmByVersion: (String) -> Unit,
    onQuickLinkClick: (String) -> Unit
) {
    var version by remember { mutableStateOf("") }

    // Collect and group quick links by name from all selected apps
    val quickLinkGroups = remember(selectedApps) {
        selectedApps
            .flatMap { app -> eu.micer.distro.data.quickLinksFromJson(app.quickLinks) }
            .groupBy { it.name }
            .mapValues { (_, links) -> links.size }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter version to download") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = version,
                    onValueChange = { version = it },
                    label = { Text("Version") },
                    placeholder = { Text("e.g. 1.0.0") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Show quick links if available
                if (quickLinkGroups.isNotEmpty()) {
                    Text(
                        text = "Quick select:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickLinkGroups.forEach { (name, count) ->
                            OutlinedButton(
                                onClick = { onQuickLinkClick(name) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (count > 1) "$name ($count)" else name, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmByVersion(version) },
                enabled = version.isNotBlank()
            ) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppItem(
    appWithStatus: AppConfigWithStatus,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    selected: Boolean = false,
    selectionMode: Boolean = false
) {

    val app = appWithStatus.config
    val installedInfo = appWithStatus.installedInfo

    // Display name priority: name (user-provided or auto-filled) > appLabel (from APK) > fallback
    val displayName = app.name.ifBlank { app.appLabel ?: "{App name not provided}" }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                // App Name
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Package Name
                if (app.packageName != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Installation Status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (installedInfo.isInstalled)
                            Icons.Filled.CheckCircle
                        else
                            Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = if (installedInfo.isInstalled)
                            Success
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (installedInfo.isInstalled) {
                            "Installed: ${installedInfo.versionName ?: "Unknown"}"
                        } else {
                            "Not installed"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (installedInfo.isInstalled)
                            Success
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (installedInfo.isInstalled) FontWeight.Medium else FontWeight.Normal
                    )
                }

                // Downloaded Version (from last download)
                if (app.versionName != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Last downloaded: ${app.versionName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // URL Pattern
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = app.urlPattern,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "No apps configured",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Go to App Configuration to add your first app",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
