package com.frictionscroll.ui.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.frictionscroll.util.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var hasOverlay by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }
    var hasAccessibility by remember { mutableStateOf(PermissionHelper.hasAccessibilityPermission(context)) }

    // Re-check on resume
    LaunchedEffect(Unit) {
        // Simple poll every time composition restarts
        hasOverlay = PermissionHelper.hasOverlayPermission(context)
        hasAccessibility = PermissionHelper.hasAccessibilityPermission(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permissions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "FrictionScroll needs two permissions to work. " +
                    "It monitors scroll events in your selected apps to detect rapid scrolling patterns. " +
                    "It does NOT read text content, keystrokes, messages, or any personal data.",
                style = MaterialTheme.typography.bodyMedium
            )

            // Overlay permission
            PermissionCard(
                title = "Display Over Other Apps",
                description = "Required to show the friction overlay when rapid scrolling is detected.",
                granted = hasOverlay,
                onOpenSettings = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            )

            // Accessibility permission
            PermissionCard(
                title = "Scroll Monitoring Service",
                description = "Required to detect scroll events in your selected apps. FrictionScroll only monitors scroll actions — nothing else.",
                granted = hasAccessibility,
                steps = if (!hasAccessibility) listOf(
                    "1. Tap \"Open Settings\" below",
                    "2. Look for \"Installed apps\" or \"Downloaded apps\"",
                    "3. Find and tap \"FrictionScroll\"",
                    "4. Toggle the switch ON",
                    "5. Tap \"Allow\" on the confirmation dialog"
                ) else null,
                onOpenSettings = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    hasOverlay = PermissionHelper.hasOverlayPermission(context)
                    hasAccessibility = PermissionHelper.hasAccessibilityPermission(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Refresh Status") }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    steps: List<String>? = null,
    onOpenSettings: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                val statusColor = if (granted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
                Text(
                    if (granted) "Granted" else "Not Granted",
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodySmall)
            if (!granted) {
                if (steps != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "How to enable:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            steps.forEach { step ->
                                Text(
                                    step,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onOpenSettings) {
                    Text("Open Settings")
                }
            }
        }
    }
}
