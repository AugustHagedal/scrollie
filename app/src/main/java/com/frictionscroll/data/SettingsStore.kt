package com.frictionscroll.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "friction_scroll_settings")

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SELECTED_APPS = stringSetPreferencesKey("selected_apps")
        val ENABLED = booleanPreferencesKey("enabled")
        val BURST_N = intPreferencesKey("burst_n")
        val BURST_WINDOW_SEC = intPreferencesKey("burst_window_sec")
        val DELAY_MS = longPreferencesKey("delay_ms")
        val COOLDOWN_MS = longPreferencesKey("cooldown_ms")
        val SNOOZE_UNTIL = longPreferencesKey("snooze_until")
        val TRIGGERS_TODAY = intPreferencesKey("triggers_today")
        val LAST_TRIGGER_TIME = longPreferencesKey("last_trigger_time")
        val LAST_TRIGGER_DATE = stringPreferencesKey("last_trigger_date")
    }

    val selectedApps: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_APPS] ?: emptySet()
    }

    val enabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ENABLED] ?: false
    }

    val burstN: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.BURST_N] ?: 5
    }

    val burstWindowSec: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.BURST_WINDOW_SEC] ?: 10
    }

    val delayMs: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.DELAY_MS] ?: 2000L
    }

    val cooldownMs: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.COOLDOWN_MS] ?: 2000L
    }

    val snoozeUntil: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.SNOOZE_UNTIL] ?: 0L
    }

    val triggersToday: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.TRIGGERS_TODAY] ?: 0
    }

    val lastTriggerTime: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_TRIGGER_TIME] ?: 0L
    }

    suspend fun setSelectedApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.SELECTED_APPS] = apps }
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ENABLED] = enabled }
    }

    suspend fun setBurstN(n: Int) {
        context.dataStore.edit { it[Keys.BURST_N] = n }
    }

    suspend fun setBurstWindowSec(sec: Int) {
        context.dataStore.edit { it[Keys.BURST_WINDOW_SEC] = sec }
    }

    suspend fun setDelayMs(ms: Long) {
        context.dataStore.edit { it[Keys.DELAY_MS] = ms }
    }

    suspend fun setCooldownMs(ms: Long) {
        context.dataStore.edit { it[Keys.COOLDOWN_MS] = ms }
    }

    suspend fun setSnoozeUntil(epochMs: Long) {
        context.dataStore.edit { it[Keys.SNOOZE_UNTIL] = epochMs }
    }

    suspend fun recordTrigger(timeMs: Long, dateString: String) {
        context.dataStore.edit { prefs ->
            val lastDate = prefs[Keys.LAST_TRIGGER_DATE] ?: ""
            if (lastDate != dateString) {
                prefs[Keys.TRIGGERS_TODAY] = 1
            } else {
                prefs[Keys.TRIGGERS_TODAY] = (prefs[Keys.TRIGGERS_TODAY] ?: 0) + 1
            }
            prefs[Keys.LAST_TRIGGER_TIME] = timeMs
            prefs[Keys.LAST_TRIGGER_DATE] = dateString
        }
    }

    suspend fun clearStats() {
        context.dataStore.edit { prefs ->
            prefs[Keys.TRIGGERS_TODAY] = 0
            prefs[Keys.LAST_TRIGGER_TIME] = 0L
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { prefs ->
            prefs[Keys.BURST_N] = 5
            prefs[Keys.BURST_WINDOW_SEC] = 10
            prefs[Keys.DELAY_MS] = 2000L
            prefs[Keys.COOLDOWN_MS] = 2000L
        }
    }
}
