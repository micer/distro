package eu.micer.distro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.micer.distro.ui.theme.BrightHighlight
import eu.micer.distro.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppScreen(
    appName: String = "",
    urlPattern: String = "",
    packageName: String = "",
    onNavigateBack: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(appName) }
    var pattern by remember { mutableStateOf(urlPattern) }
    var packageNameValue by remember { mutableStateOf(packageName) }
    var nameError by remember { mutableStateOf(false) }
    var patternError by remember { mutableStateOf(false) }
    
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
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                            onSave(finalName, pattern.trim(), finalPackageName)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
