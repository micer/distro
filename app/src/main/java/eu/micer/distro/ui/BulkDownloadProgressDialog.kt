package eu.micer.distro.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import eu.micer.distro.ui.theme.Success
import eu.micer.distro.utils.DownloadState
import eu.micer.distro.viewmodel.BulkDownloadItem
import eu.micer.distro.viewmodel.BulkDownloadState

@Composable
fun BulkDownloadProgressDialog(
    bulkState: BulkDownloadState,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { 
            if (!bulkState.isActive) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !bulkState.isActive,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (bulkState.isActive) "Downloading Apps" else "Download Complete",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (!bulkState.isActive) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Overall Progress
                OverallProgressSection(bulkState)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // App List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bulkState.items, key = { it.appId }) { item ->
                        AppDownloadItem(item)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Actions
                if (bulkState.isActive) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel All")
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallProgressSection(bulkState: BulkDownloadState) {
    val progress = if (bulkState.totalCount > 0) {
        bulkState.completedCount.toFloat() / bulkState.totalCount.toFloat()
    } else {
        0f
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "overall_progress"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Overall Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${bulkState.completedCount} / ${bulkState.totalCount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
            )
            
            if (bulkState.failedCount > 0) {
                Text(
                    text = "⚠️ ${bulkState.failedCount} failed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AppDownloadItem(item: BulkDownloadItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = when (item.state) {
            is DownloadState.Success -> CardDefaults.cardColors(
                containerColor = Success.copy(alpha = 0.1f)
            )
            is DownloadState.Error -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
            else -> CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Icon(
                imageVector = when (item.state) {
                    is DownloadState.Success -> Icons.Filled.CheckCircle
                    is DownloadState.Error -> Icons.Filled.Error
                    is DownloadState.Downloading -> Icons.Filled.Download
                    else -> Icons.Outlined.Circle
                },
                contentDescription = null,
                tint = when (item.state) {
                    is DownloadState.Success -> Success
                    is DownloadState.Error -> MaterialTheme.colorScheme.error
                    is DownloadState.Downloading -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )
            
            // App Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = when (val state = item.state) {
                        is DownloadState.Idle -> "Waiting..."
                        is DownloadState.Downloading -> {
                            if (state.isIndeterminate) {
                                "Downloading... ${formatBytes(state.downloadedBytes)}"
                            } else {
                                "Downloading... ${String.format("%.2f%%", state.progress * 100)}"
                            }
                        }
                        is DownloadState.Success -> "✓ Complete"
                        is DownloadState.Error -> "✗ ${state.message}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (item.state) {
                        is DownloadState.Error -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Progress bar for downloading
                if (item.state is DownloadState.Downloading) {
                    val downloadState = item.state as DownloadState.Downloading
                    if (downloadState.isIndeterminate) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )
                    } else {
                        val animatedProgress by animateFloatAsState(
                            targetValue = downloadState.progress,
                            animationSpec = tween(durationMillis = 300),
                            label = "item_progress"
                        )
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
