package com.frictionscroll.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore
) : ViewModel() {
    val burstN = settingsStore.burstN
    val burstWindowSec = settingsStore.burstWindowSec

    suspend fun setBurstN(n: Int) = settingsStore.setBurstN(n)
    suspend fun setBurstWindowSec(sec: Int) = settingsStore.setBurstWindowSec(sec)
    suspend fun resetToDefaults() = settingsStore.resetToDefaults()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val burstN by viewModel.burstN.collectAsStateWithLifecycle(initialValue = 5)
    val burstWindowSec by viewModel.burstWindowSec.collectAsStateWithLifecycle(initialValue = 10)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("settings") },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "adjust how scroll pauses are detected",
                style = MaterialTheme.typography.bodyMedium
            )

            // Burst count (N): 3–12
            ConfigSlider(
                label = "scroll count to trigger",
                value = burstN.toFloat(),
                valueRange = 3f..12f,
                steps = 8,
                valueLabel = "$burstN scrolls",
                onValueChange = { scope.launch { viewModel.setBurstN(it.roundToInt()) } }
            )

            // Burst window (T): 5–25s
            ConfigSlider(
                label = "detection window",
                value = burstWindowSec.toFloat(),
                valueRange = 5f..25f,
                steps = 3,
                valueLabel = "${burstWindowSec}s",
                onValueChange = { scope.launch { viewModel.setBurstWindowSec(it.roundToInt()) } }
            )

            // Fixed values — read-only
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "fixed values",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("pause duration", style = MaterialTheme.typography.bodyMedium)
                        Text("3s", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("cooldown", style = MaterialTheme.typography.bodyMedium)
                        Text("15s", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { scope.launch { viewModel.resetToDefaults() } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("reset to defaults") }
        }
    }
}

@Composable
private fun ConfigSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(valueLabel, style = MaterialTheme.typography.bodyMedium)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
