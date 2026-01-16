package eu.micer.distro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.micer.distro.data.QuickLink
import eu.micer.distro.data.quickLinksFromJson
import eu.micer.distro.data.quickLinksToJson
import eu.micer.distro.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppScreen(
    appName: String = "",
    urlPattern: String = "",
    packageName: String = "",
    quickLinksJson: String? = null,
    onNavigateBack: () -> Unit,
    onSave: (String, String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(appName) }
    var pattern by remember { mutableStateOf(urlPattern) }
    var packageNameValue by remember { mutableStateOf(packageName) }
    var nameError by remember { mutableStateOf(false) }
    var patternError by remember { mutableStateOf(false) }
    var quickLinks by remember {
        mutableStateOf(quickLinksFromJson(quickLinksJson))
    }
    var showAddQuickLinkDialog by remember { mutableStateOf(false) }

    val previewUrl = pattern.replace("{version}", "1.0.0")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (appName.isEmpty()) "Add App" else "Edit App") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configure your APK distribution source",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("App Name (Optional)") },
                placeholder = { Text("Leave empty to auto-fill from APK") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = {
                    Text("Name will be extracted from APK on first download")
                },
                singleLine = true
            )
            
            OutlinedTextField(
                value = packageNameValue,
                onValueChange = { packageNameValue = it },
                label = { Text("Package Name (Optional)") },
                placeholder = { Text("com.example.app") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text("For detecting installations from any source (e.g., Play Store)")
                },
                singleLine = true
            )
            
            OutlinedTextField(
                value = pattern,
                onValueChange = {
                    pattern = it
                    patternError = false
                },
                label = { Text("URL Pattern") },
                placeholder = { Text("https://example.com/apk-{version}.apk") },
                modifier = Modifier.fillMaxWidth(),
                isError = patternError,
                supportingText = if (patternError) {
                    { Text("URL must contain {version} placeholder") }
                } else {
                    { Text("Use {version} as placeholder for the version") }
                },
                minLines = 2,
                maxLines = 4
            )
            
            if (pattern.contains("{version}")) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = previewUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Quick Links Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Links",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { showAddQuickLinkDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Quick Link")
                        }
                    }

                    if (quickLinks.isEmpty()) {
                        Text(
                            text = "No quick links added",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickLinks.forEach { quickLink ->
                                QuickLinkItem(
                                    quickLink = quickLink,
                                    onDelete = {
                                        quickLinks = quickLinks.filter { it != quickLink }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Quick links provide direct download URLs for specific builds (e.g., latest, RC, NIGHTLY).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        var hasError = false

                        if (pattern.isBlank() || !pattern.contains("{version}")) {
                            patternError = true
                            hasError = true
                        }

                        if (!hasError) {
                            // Use name if provided, otherwise leave blank to be filled from APK
                            val finalName = name.trim()
                            val finalPackageName = packageNameValue.trim()
                            val finalQuickLinks = if (quickLinks.isEmpty()) null else quickLinksToJson(quickLinks)
                            onSave(finalName, pattern.trim(), finalPackageName, finalQuickLinks)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }

    if (showAddQuickLinkDialog) {
        AddQuickLinkDialog(
            onDismiss = { showAddQuickLinkDialog = false },
            onConfirm = { name, link ->
                quickLinks = quickLinks + QuickLink(name.trim(), link.trim())
                showAddQuickLinkDialog = false
            }
        )
    }
}

@Composable
fun QuickLinkItem(
    quickLink: QuickLink,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quickLink.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = quickLink.link,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddQuickLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var linkError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Quick Link") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Name") },
                    placeholder = { Text("e.g., latest, RC, NIGHTLY") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Name is required") }
                    } else {
                        { Text("Display name for the quick link") }
                    },
                    singleLine = true
                )
                OutlinedTextField(
                    value = link,
                    onValueChange = {
                        link = it
                        linkError = false
                    },
                    label = { Text("Direct Download URL") },
                    placeholder = { Text("https://example.com/app-latest.apk") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = linkError,
                    supportingText = if (linkError) {
                        { Text("URL is required") }
                    } else {
                        { Text("Direct URL to download the APK") }
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (name.isBlank()) {
                        nameError = true
                        hasError = true
                    }
                    if (link.isBlank()) {
                        linkError = true
                        hasError = true
                    }
                    if (!hasError) {
                        onConfirm(name, link)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
