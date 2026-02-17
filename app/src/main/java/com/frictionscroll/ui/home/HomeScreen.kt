package com.frictionscroll.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frictionscroll.data.SettingsStore
import com.frictionscroll.util.PermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsStore: SettingsStore
) : ViewModel() {
    val enabled = settingsStore.enabled
    val triggersToday = settingsStore.triggersToday
    val lastTriggerTime = settingsStore.lastTriggerTime
    val selectedApps = settingsStore.selectedApps

    suspend fun setEnabled(value: Boolean) = settingsStore.setEnabled(value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAppPicker: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToDebug: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle(initialValue = false)
    val triggersToday by viewModel.triggersToday.collectAsStateWithLifecycle(initialValue = 0)
    val lastTriggerTime by viewModel.lastTriggerTime.collectAsStateWithLifecycle(initialValue = 0L)
    val selectedApps by viewModel.selectedApps.collectAsStateWithLifecycle(initialValue = emptySet())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val hasOverlay = PermissionHelper.hasOverlayPermission(context)
    val hasAccessibility = PermissionHelper.hasAccessibilityPermission(context)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("FrictionScroll") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master toggle
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Friction Enabled", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${selectedApps.size} app(s) selected",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { scope.launch { viewModel.setEnabled(it) } }
                    )
                }
            }

            // Permission status
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Permissions", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermissionChip(label = "Overlay", granted = hasOverlay)
                        PermissionChip(label = "Service", granted = hasAccessibility)
                    }
                }
            }

            // Stats
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Triggers: $triggersToday")
                    if (lastTriggerTime > 0) {
                        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(Date(lastTriggerTime))
                        Text("Last trigger: $timeStr")
                    }
                }
            }

            // Navigation buttons
            Button(
                onClick = onNavigateToAppPicker,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Select Apps") }

            Button(
                onClick = onNavigateToConfig,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Configure Burst Settings") }

            OutlinedButton(
                onClick = onNavigateToPermissions,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Permissions") }

            OutlinedButton(
                onClick = onNavigateToDebug,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Debug Info") }
        }
    }
}

@Composable
private fun PermissionChip(label: String, granted: Boolean) {
    val color = if (granted) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.error
    SuggestionChip(
        onClick = {},
        label = { Text("$label: ${if (granted) "OK" else "Missing"}") },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        )
    )
}
