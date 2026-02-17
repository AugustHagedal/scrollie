package com.frictionscroll.ui.debug

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frictionscroll.data.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val settingsStore: SettingsStore
) : ViewModel() {
    val enabled = settingsStore.enabled
    val triggersToday = settingsStore.triggersToday
    val lastTriggerTime = settingsStore.lastTriggerTime
    val burstN = settingsStore.burstN
    val burstWindowSec = settingsStore.burstWindowSec
    val delayMs = settingsStore.delayMs
    val cooldownMs = settingsStore.cooldownMs
    val snoozeUntil = settingsStore.snoozeUntil
    val selectedApps = settingsStore.selectedApps

    suspend fun clearStats() = settingsStore.clearStats()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onBack: () -> Unit,
    viewModel: DebugViewModel = hiltViewModel()
) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle(initialValue = false)
    val triggersToday by viewModel.triggersToday.collectAsStateWithLifecycle(initialValue = 0)
    val lastTriggerTime by viewModel.lastTriggerTime.collectAsStateWithLifecycle(initialValue = 0L)
    val burstN by viewModel.burstN.collectAsStateWithLifecycle(initialValue = 5)
    val burstWindowSec by viewModel.burstWindowSec.collectAsStateWithLifecycle(initialValue = 10)
    val delayMs by viewModel.delayMs.collectAsStateWithLifecycle(initialValue = 2000L)
    val cooldownMs by viewModel.cooldownMs.collectAsStateWithLifecycle(initialValue = 2000L)
    val snoozeUntil by viewModel.snoozeUntil.collectAsStateWithLifecycle(initialValue = 0L)
    val selectedApps by viewModel.selectedApps.collectAsStateWithLifecycle(initialValue = emptySet())
    val scope = rememberCoroutineScope()

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Info") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("State", style = MaterialTheme.typography.titleMedium)
            DebugRow("Enabled", "$enabled")
            DebugRow("Triggers today", "$triggersToday")
            DebugRow("Last trigger",
                if (lastTriggerTime > 0) dateFormat.format(Date(lastTriggerTime)) else "Never"
            )
            DebugRow("Snooze until",
                if (snoozeUntil > 0) dateFormat.format(Date(snoozeUntil)) else "Not snoozed"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Configuration", style = MaterialTheme.typography.titleMedium)
            DebugRow("Burst count (N)", "$burstN")
            DebugRow("Burst window", "${burstWindowSec}s")
            DebugRow("Overlay delay", "${delayMs}ms")
            DebugRow("Cooldown", "${cooldownMs}ms")

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Selected Apps (${selectedApps.size})", style = MaterialTheme.typography.titleMedium)
            selectedApps.forEach { pkg ->
                Text(pkg, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { scope.launch { viewModel.clearStats() } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Clear Stats") }
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
