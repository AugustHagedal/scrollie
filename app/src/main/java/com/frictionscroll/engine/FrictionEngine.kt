package com.frictionscroll.engine

enum class TriggerResult {
    TRIGGERED,
    DEBOUNCED,
    COOLDOWN,
    SNOOZED,
    RATE_LIMITED,
    NOT_READY
}

class FrictionEngine(private val clock: Clock) {

    private var burstN: Int = 5
    private var burstWindowMs: Long = 10_000L
    var cooldownMs: Long = 15_000L
        private set
    private val debounceMs: Long = 150L
    private var maxTriggersPerMinute: Int = 4

    private var currentPackage: String? = null
    private val scrollTimestamps = mutableListOf<Long>()
    private var lastAcceptedScroll: Long = 0L
    private var lastTriggerTime: Long = 0L
    private var snoozeUntil: Long = 0L
    private val triggerTimestamps = mutableListOf<Long>()

    fun configure(n: Int, windowSec: Int, cooldownMs: Long) {
        this.burstN = n
        this.burstWindowMs = windowSec * 1000L
        this.cooldownMs = cooldownMs
    }

    fun setSnoozeUntil(epochMs: Long) {
        this.snoozeUntil = epochMs
    }

    fun onScrollEvent(packageName: String): TriggerResult {
        val now = clock.now()

        // Package change → reset rolling window
        if (packageName != currentPackage) {
            currentPackage = packageName
            scrollTimestamps.clear()
            lastAcceptedScroll = 0L
        }

        // Snooze check
        if (snoozeUntil > 0 && now < snoozeUntil) {
            return TriggerResult.SNOOZED
        }

        // Debounce check
        if (now - lastAcceptedScroll < debounceMs) {
            return TriggerResult.DEBOUNCED
        }

        // Cooldown check
        if (lastTriggerTime > 0 && now - lastTriggerTime < cooldownMs) {
            return TriggerResult.COOLDOWN
        }

        lastAcceptedScroll = now

        // Add to rolling window and prune old entries
        scrollTimestamps.add(now)
        val windowStart = now - burstWindowMs
        scrollTimestamps.removeAll { it < windowStart }

        // Check if burst threshold met
        return if (scrollTimestamps.size >= burstN) {
            // Rate limit check: prune trigger timestamps older than 60s
            triggerTimestamps.removeAll { it < now - 60_000L }
            if (triggerTimestamps.size >= maxTriggersPerMinute) {
                return TriggerResult.RATE_LIMITED
            }

            lastTriggerTime = now
            triggerTimestamps.add(now)
            scrollTimestamps.clear()
            TriggerResult.TRIGGERED
        } else {
            TriggerResult.NOT_READY
        }
    }

    fun reset() {
        currentPackage = null
        scrollTimestamps.clear()
        lastAcceptedScroll = 0L
        lastTriggerTime = 0L
        snoozeUntil = 0L
        triggerTimestamps.clear()
    }
}
