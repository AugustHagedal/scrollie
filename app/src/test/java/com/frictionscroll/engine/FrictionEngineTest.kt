package com.frictionscroll.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeClock(var timeMs: Long = 0L) : Clock {
    override fun now(): Long = timeMs
    fun advance(ms: Long) { timeMs += ms }
}

class FrictionEngineTest {

    private lateinit var clock: FakeClock
    private lateinit var engine: FrictionEngine

    @Before
    fun setUp() {
        clock = FakeClock(1_000_000L)
        engine = FrictionEngine(clock)
        engine.configure(n = 5, windowSec = 10, cooldownMs = 15000L)
    }

    @Test
    fun `N scrolls in T seconds triggers`() {
        val pkg = "com.example.app"
        for (i in 1..4) {
            assertEquals(TriggerResult.NOT_READY, engine.onScrollEvent(pkg))
            clock.advance(200)
        }
        assertEquals(TriggerResult.TRIGGERED, engine.onScrollEvent(pkg))
    }

    @Test
    fun `scroll within 150ms is debounced`() {
        val pkg = "com.example.app"
        assertEquals(TriggerResult.NOT_READY, engine.onScrollEvent(pkg))
        clock.advance(100) // only 100ms, within debounce
        assertEquals(TriggerResult.DEBOUNCED, engine.onScrollEvent(pkg))
    }

    @Test
    fun `trigger during cooldown returns COOLDOWN`() {
        val pkg = "com.example.app"
        // Trigger first
        for (i in 1..5) {
            engine.onScrollEvent(pkg)
            clock.advance(200)
        }
        // Now within cooldown period (15s), try to scroll again
        clock.advance(200)
        assertEquals(TriggerResult.COOLDOWN, engine.onScrollEvent(pkg))
    }

    @Test
    fun `snooze active returns SNOOZED`() {
        val pkg = "com.example.app"
        engine.setSnoozeUntil(clock.now() + 300_000L) // snooze for 5 min
        assertEquals(TriggerResult.SNOOZED, engine.onScrollEvent(pkg))
    }

    @Test
    fun `snooze expired allows normal operation`() {
        val pkg = "com.example.app"
        engine.setSnoozeUntil(clock.now() + 1000L)
        clock.advance(1500L) // past snooze
        assertEquals(TriggerResult.NOT_READY, engine.onScrollEvent(pkg))
    }

    @Test
    fun `package switch resets window`() {
        val pkg1 = "com.example.app1"
        val pkg2 = "com.example.app2"

        // Build up 4 scrolls on app1
        for (i in 1..4) {
            engine.onScrollEvent(pkg1)
            clock.advance(200)
        }

        // Switch to app2 — counter should reset
        assertEquals(TriggerResult.NOT_READY, engine.onScrollEvent(pkg2))

        // Need full 5 scrolls on app2
        for (i in 1..3) {
            clock.advance(200)
            engine.onScrollEvent(pkg2)
        }
        clock.advance(200)
        assertEquals(TriggerResult.TRIGGERED, engine.onScrollEvent(pkg2))
    }

    @Test
    fun `N-1 scrolls does not trigger`() {
        val pkg = "com.example.app"
        for (i in 1..4) {
            val result = engine.onScrollEvent(pkg)
            assertEquals(TriggerResult.NOT_READY, result)
            clock.advance(200)
        }
        // Only 4 scrolls sent, should not have triggered
    }

    @Test
    fun `scrolls outside window are pruned`() {
        val pkg = "com.example.app"
        // Send 3 scrolls
        for (i in 1..3) {
            engine.onScrollEvent(pkg)
            clock.advance(200)
        }
        // Jump past the window
        clock.advance(11_000)
        // Old scrolls should be pruned, start fresh
        assertEquals(TriggerResult.NOT_READY, engine.onScrollEvent(pkg))
    }

    @Test
    fun `cooldown expires and allows new trigger`() {
        val pkg = "com.example.app"
        // First trigger
        for (i in 1..5) {
            engine.onScrollEvent(pkg)
            clock.advance(200)
        }
        // Wait past 15s cooldown
        clock.advance(16_000)
        // Should be able to build toward new trigger
        assertEquals(TriggerResult.NOT_READY, engine.onScrollEvent(pkg))
    }

    @Test
    fun `reset clears all state`() {
        val pkg = "com.example.app"
        for (i in 1..3) {
            engine.onScrollEvent(pkg)
            clock.advance(200)
        }
        engine.reset()
        // After reset, starts fresh
        assertEquals(TriggerResult.NOT_READY, engine.onScrollEvent(pkg))
    }

    @Test
    fun `4 triggers in 60s allowed, 5th returns RATE_LIMITED`() {
        // Use short cooldown so 4 triggers fit within 60s window
        engine.configure(n = 5, windowSec = 10, cooldownMs = 2000L)
        val pkg = "com.example.app"
        // Trigger 4 times within 60 seconds
        for (t in 1..4) {
            for (i in 1..5) {
                engine.onScrollEvent(pkg)
                clock.advance(200)
            }
            // Wait past 2s cooldown between triggers
            clock.advance(2500)
        }
        // 5th trigger attempt — should be rate limited
        for (i in 1..4) {
            engine.onScrollEvent(pkg)
            clock.advance(200)
        }
        assertEquals(TriggerResult.RATE_LIMITED, engine.onScrollEvent(pkg))
    }

    @Test
    fun `rate limit resets after 60s window passes`() {
        engine.configure(n = 5, windowSec = 10, cooldownMs = 2000L)
        val pkg = "com.example.app"
        // Trigger 4 times
        for (t in 1..4) {
            for (i in 1..5) {
                engine.onScrollEvent(pkg)
                clock.advance(200)
            }
            clock.advance(2500)
        }
        // Wait for 60s window to clear from the first trigger
        clock.advance(60_000)
        // Should be able to trigger again
        for (i in 1..4) {
            engine.onScrollEvent(pkg)
            clock.advance(200)
        }
        assertEquals(TriggerResult.TRIGGERED, engine.onScrollEvent(pkg))
    }

    @Test
    fun `rate limit resets on reset()`() {
        engine.configure(n = 5, windowSec = 10, cooldownMs = 2000L)
        val pkg = "com.example.app"
        // Trigger 4 times
        for (t in 1..4) {
            for (i in 1..5) {
                engine.onScrollEvent(pkg)
                clock.advance(200)
            }
            clock.advance(2500)
        }
        engine.reset()
        // After reset, should be able to trigger again
        for (i in 1..4) {
            engine.onScrollEvent(pkg)
            clock.advance(200)
        }
        assertEquals(TriggerResult.TRIGGERED, engine.onScrollEvent(pkg))
    }
}
