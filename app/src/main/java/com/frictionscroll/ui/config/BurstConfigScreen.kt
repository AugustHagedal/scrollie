package com.frictionscroll.ui.config

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
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@HiltViewModel
class BurstConfigViewModel @Inject constructor(
    private val settingsStore: SettingsStore
) : ViewModel() {
    val burstN = settingsStore.burstN
    val burstWindowSec = settingsStore.burstWindowSec
    val delayMs = settingsStore.delayMs
    val cooldownMs = settingsStore.cooldownMs

    suspend fun setBurstN(n: Int) = settingsStore.setBurstN(n)
    suspend fun setBurstWindowSec(sec: Int) = settingsStore.setBurstWindowSec(sec)
    suspend fun setDelayMs(ms: Long) = settingsStore.setDelayMs(ms)
    suspend fun setCooldownMs(ms: Long) = settingsStore.setCooldownMs(ms)
    suspend fun resetToDefaults() = settingsStore.resetToDefaults()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurstConfigScreen(
    onBack: () -> Unit,
    viewModel: BurstConfigViewModel = hiltViewModel()
) {
    val burstN by viewModel.burstN.collectAsStateWithLifecycle(initialValue = 5)
    val burstWindowSec by viewModel.burstWindowSec.collectAsStateWithLifecycle(initialValue = 10)
    val delayMs by viewModel.delayMs.collectAsStateWithLifecycle(initialValue = 2000L)
    val cooldownMs by viewModel.cooldownMs.collectAsStateWithLifecycle(initialValue = 2000L)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Burst Settings") },
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
                "Adjust how FrictionScroll detects rapid scrolling",
                style = MaterialTheme.typography.bodyMedium
            )

            // Burst count (N)
            ConfigSlider(
                label = "Scroll count to trigger",
                value = burstN.toFloat(),
                valueRange = 1f..20f,
                steps = 18,
                valueLabel = "$burstN scrolls",
                onValueChange = { scope.launch { viewModel.setBurstN(it.roundToInt()) } }
            )

            // Burst window (T)
            ConfigSlider(
                label = "Detection window",
                value = burstWindowSec.toFloat(),
                valueRange = 5f..60f,
                steps = 10,
                valueLabel = "${burstWindowSec}s",
                onValueChange = { scope.launch { viewModel.setBurstWindowSec(it.roundToInt()) } }
            )

            // Delay
            ConfigSlider(
                label = "Overlay delay",
                value = delayMs.toFloat(),
                valueRange = 500f..10000f,
                steps = 18,
                valueLabel = "${delayMs / 1000.0}s",
                onValueChange = { scope.launch { viewModel.setDelayMs(it.roundToLong()) } }
            )

            // Cooldown
            ConfigSlider(
                label = "Cooldown after trigger",
                value = cooldownMs.toFloat(),
                valueRange = 500f..10000f,
                steps = 18,
                valueLabel = "${cooldownMs / 1000.0}s",
                onValueChange = { scope.launch { viewModel.setCooldownMs(it.roundToLong()) } }
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { scope.launch { viewModel.resetToDefaults() } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset to Defaults") }
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
