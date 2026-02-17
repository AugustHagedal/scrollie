package com.frictionscroll.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onNavigateToStats: () -> Unit,
    onNavigateToDisclosure: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle(initialValue = true)
    val triggersToday by viewModel.triggersToday.collectAsStateWithLifecycle(initialValue = 0)
    val lastTriggerTime by viewModel.lastTriggerTime.collectAsStateWithLifecycle(initialValue = 0L)
    val selectedApps by viewModel.selectedApps.collectAsStateWithLifecycle(initialValue = emptySet())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val hasOverlay = PermissionHelper.hasOverlayPermission(context)
    val hasAccessibility = PermissionHelper.hasAccessibilityPermission(context)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("frictionscroll") })
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("awareness pause", style = MaterialTheme.typography.titleMedium)
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("permissions", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermissionChip(label = "overlay", granted = hasOverlay)
                        PermissionChip(label = "service", granted = hasAccessibility)
                    }
                }
            }

            // Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("today", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("pauses: $triggersToday")
                    if (lastTriggerTime > 0) {
                        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(Date(lastTriggerTime))
                        Text("last pause: $timeStr")
                    }
                }
            }

            // Navigation buttons
            Button(
                onClick = onNavigateToAppPicker,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("select apps") }

            Button(
                onClick = onNavigateToConfig,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("settings") }

            OutlinedButton(
                onClick = onNavigateToPermissions,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("permissions") }

            OutlinedButton(
                onClick = onNavigateToStats,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("stats") }

            OutlinedButton(
                onClick = onNavigateToDisclosure,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("about this app") }
        }
    }
}

@Composable
private fun PermissionChip(label: String, granted: Boolean) {
    val color = if (granted) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.error
    SuggestionChip(
        onClick = {},
        label = { Text("$label: ${if (granted) "ok" else "missing"}") },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        )
    )
}
