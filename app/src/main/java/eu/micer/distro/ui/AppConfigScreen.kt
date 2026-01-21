package eu.micer.distro.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.micer.distro.data.AppConfig
import eu.micer.distro.ui.theme.Primary
import eu.micer.distro.ui.theme.OnPrimary
import eu.micer.distro.viewmodel.ImportState
import com.composables.icons.fontawesome.solid.R.drawable as fontAwesomeIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConfigScreen(
    apps: List<AppConfig>,
    importState: ImportState = ImportState.Idle,
    onNavigateBack: () -> Unit,
    onAddApp: () -> Unit,
    onEditApp: (Long) -> Unit,
    onDeleteApp: (AppConfig) -> Unit,
    onImport: (String) -> Unit = {},
    onResetImportState: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle import state changes
    LaunchedEffect(importState) {
        when (importState) {
            is ImportState.Success -> {
                snackbarHostState.showSnackbar("Successfully imported ${importState.count} apps")
                onResetImportState()
            }

            is ImportState.Error -> {
                snackbarHostState.showSnackbar("Import failed: ${importState.message}")
                onResetImportState()
            }

            else -> {}
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val jsonString =
                        inputStream.bufferedReader().use { reader -> reader.readText() }
                    onImport(jsonString)
                }
            } catch (e: Exception) {
                onImport("") // This will trigger error in ViewModel or handle it here
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("App Configuration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { importLauncher.launch("application/json") }) {
                        Icon(
                            painterResource(id = fontAwesomeIcons.fontawesome_ic_file_import_solid),
                            contentDescription = "Import JSON",
                            tint = OnPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary,
                    actionIconContentColor = OnPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddApp,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add App")
            }
        }
    ) { padding ->
        if (apps.isEmpty()) {
            EmptyConfigState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(apps, key = { it.id }) { app ->
                    AppConfigItem(
                        app = app,
                        onEdit = { onEditApp(app.id) },
                        onDelete = { onDeleteApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyConfigState(modifier: Modifier = Modifier) {
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
                text = "Tap the + button to add your first app or use import to add apps from a JSON file",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConfigItem(
    app: AppConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    // Display name priority: name (user-provided or auto-filled) > appLabel (from APK) > fallback
    val displayName = app.name.ifBlank { app.appLabel ?: "{App name not provided}" }
    
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // App Name
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Package Name (if available)
                if (app.packageName != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete App") },
            text = { Text("Are you sure you want to delete \"$displayName\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}