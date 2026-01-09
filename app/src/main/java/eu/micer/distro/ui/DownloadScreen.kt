package eu.micer.distro.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import eu.micer.distro.ui.theme.BrightHighlight
import eu.micer.distro.ui.theme.PrimaryBlue
import eu.micer.distro.ui.theme.SuccessGreen
import eu.micer.distro.utils.DownloadState
import timber.log.Timber

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    appName: String,
    urlPattern: String,
    downloadState: DownloadState,
    onNavigateBack: () -> Unit,
    onDownload: (String) -> Unit,
    onResetState: () -> Unit
) {
    var versionName by remember { mutableStateOf("") }
    var versionError by remember { mutableStateOf(false) }
    
    val finalUrl = if (versionName.isNotBlank()) {
        urlPattern.replace("{version}", versionName)
    } else {
        urlPattern
    }
    
    LaunchedEffect(Unit) {
        onResetState()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appName) },
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
                    containerColor = PrimaryBlue,
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
                text = "Enter version to download and install",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            OutlinedTextField(
                value = versionName,
                onValueChange = {
                    versionName = it
                    versionError = false
                },
                label = { Text("Version Name") },
                placeholder = { Text("e.g., 1.0.0 or v1.2.3") },
                modifier = Modifier.fillMaxWidth(),
                isError = versionError,
                supportingText = if (versionError) {
                    { Text("Please enter a version name") }
                } else null,
                singleLine = true,
                enabled = downloadState !is DownloadState.Downloading
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { versionName = "latest" },
                    modifier = Modifier.weight(1f),
                    enabled = downloadState !is DownloadState.Downloading
                ) {
                    Text("Latest", maxLines = 1)
                }
                OutlinedButton(
                    onClick = { versionName = "NIGHTLY-latest" },
                    modifier = Modifier.weight(1f),
                    enabled = downloadState !is DownloadState.Downloading
                ) {
                    Text("NIGHTLY-latest", maxLines = 1)
                }
                OutlinedButton(
                    onClick = { versionName = "RC-latest" },
                    modifier = Modifier.weight(1f),
                    enabled = downloadState !is DownloadState.Downloading
                ) {
                    Text("RC-latest", maxLines = 1)
                }
            }
            
            if (versionName.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Download URL",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = finalUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            when (downloadState) {
                is DownloadState.Downloading -> {
                    // Animate the progress smoothly
                    val animatedProgress by animateFloatAsState(
                        targetValue = downloadState.progress,
                        animationSpec = tween(durationMillis = 300),
                        label = "progress"
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Downloading...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                if (downloadState.isIndeterminate) {
                                    // Show downloaded bytes for indeterminate progress
                                    Text(
                                        text = formatBytes(downloadState.downloadedBytes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                } else {
                                    // Show percentage for determinate progress
                                    Text(
                                        text = String.format("%.2f%%", animatedProgress * 100),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            if (downloadState.isIndeterminate) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
                is DownloadState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = SuccessGreen,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "✓ Download Complete",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Opening installation prompt...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                is DownloadState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = downloadState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                else -> {}
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (versionName.isBlank()) {
                        versionError = true
                    } else {
                        onDownload(versionName)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = downloadState !is DownloadState.Downloading && versionName.isNotBlank()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download & Install")
            }
        }
    }
}
