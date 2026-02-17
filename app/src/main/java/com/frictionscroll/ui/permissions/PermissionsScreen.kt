package com.frictionscroll.ui.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

    LaunchedEffect(Unit) {
        hasOverlay = PermissionHelper.hasOverlayPermission(context)
        hasAccessibility = PermissionHelper.hasAccessibilityPermission(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("permissions") },
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
            // Overlay permission
            PermissionCard(
                title = "display over other apps",
                description = "required to show the pause overlay.",
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
                title = "scroll monitoring service",
                description = "required to detect scroll events in your selected apps.",
                granted = hasAccessibility,
                steps = if (!hasAccessibility) listOf(
                    "1. tap \"open settings\" below",
                    "2. look for \"installed apps\" or \"downloaded apps\"",
                    "3. find and tap \"FrictionScroll\"",
                    "4. toggle the switch on",
                    "5. tap \"allow\" on the confirmation dialog"
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("refresh status") }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                val statusColor = if (granted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
                Text(
                    if (granted) "granted" else "not granted",
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
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "how to enable:",
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
                Button(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("open settings")
                }
            }
        }
    }
}
