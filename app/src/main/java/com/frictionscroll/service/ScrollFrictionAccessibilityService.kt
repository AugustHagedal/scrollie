package com.frictionscroll.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.frictionscroll.data.SettingsStore
import com.frictionscroll.engine.FrictionEngine
import com.frictionscroll.engine.SystemClock
import com.frictionscroll.engine.TriggerResult
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class ScrollFrictionAccessibilityService : AccessibilityService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ServiceEntryPoint {
        fun settingsStore(): SettingsStore
    }

    private lateinit var settingsStore: SettingsStore
    private lateinit var engine: FrictionEngine
    private lateinit var overlayController: OverlayController
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var selectedApps: Set<String> = emptySet()
    private var enabled: Boolean = false
    private var delayMs: Long = 2000L
    private var currentForegroundPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java
        )
        settingsStore = entryPoint.settingsStore()
        engine = FrictionEngine(SystemClock())
        overlayController = OverlayController(this)

        // Collect settings changes
        serviceScope.launch {
            settingsStore.selectedApps.collect { selectedApps = it }
        }
        serviceScope.launch {
            settingsStore.enabled.collect { enabled = it }
        }
        serviceScope.launch {
            settingsStore.delayMs.collect { delayMs = it }
        }
        serviceScope.launch {
            settingsStore.snoozeUntil.collect { engine.setSnoozeUntil(it) }
        }
        serviceScope.launch {
            kotlinx.coroutines.flow.combine(
                settingsStore.burstN,
                settingsStore.burstWindowSec,
                settingsStore.cooldownMs
            ) { n, windowSec, cooldownMs ->
                Triple(n, windowSec, cooldownMs)
            }.collect { (n, windowSec, cooldownMs) ->
                engine.configure(n, windowSec, cooldownMs)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !enabled) return

        val packageName = event.packageName?.toString() ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                currentForegroundPackage = packageName
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (packageName !in selectedApps) return
                if (overlayController.isShowing) return

                val result = engine.onScrollEvent(packageName)
                if (result == TriggerResult.TRIGGERED) {
                    onTrigger()
                }
            }
        }
    }

    private fun onTrigger() {
        serviceScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val now = System.currentTimeMillis()
            settingsStore.recordTrigger(now, dateFormat.format(Date(now)))
        }

        overlayController.show(
            delayMs = delayMs,
            onDisable = {
                serviceScope.launch { settingsStore.setEnabled(false) }
            },
            onSnooze = {
                val snoozeUntil = System.currentTimeMillis() + 5 * 60 * 1000L
                serviceScope.launch { settingsStore.setSnoozeUntil(snoozeUntil) }
            }
        )
    }

    override fun onInterrupt() {
        overlayController.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayController.hide()
        serviceScope.cancel()
    }
}
