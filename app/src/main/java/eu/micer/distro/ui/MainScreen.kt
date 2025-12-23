package eu.micer.distro.ui

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import eu.micer.distro.ui.theme.PrimaryBlue
import eu.micer.distro.ui.theme.SuccessGreen
import eu.micer.distro.viewmodel.AppConfigWithStatus
import com.composables.icons.fontawesome.solid.R.drawable as fontAwesomeIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    appsWithStatus: List<AppConfigWithStatus>,
    onAppClick: (Long) -> Unit,
    onNavigateToConfig: () -> Unit,
    onRefreshInstallationStatus: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
                    containerColor = PrimaryBlue,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    actionIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },

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
                    AppItem(
                        appWithStatus = appWithStatus,
                        onClick = { onAppClick(appWithStatus.config.id) }
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppItem(
    appWithStatus: AppConfigWithStatus,
    onClick: () -> Unit
) {

    val app = appWithStatus.config
    val installedInfo = appWithStatus.installedInfo

    // Display name priority: name (user-provided or auto-filled) > appLabel (from APK) > fallback
    val displayName = app.name.ifBlank { app.appLabel ?: "{App name not provided}" }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
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
                            SuccessGreen
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
                            SuccessGreen
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
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2
                )
            }


        }
    }


}
