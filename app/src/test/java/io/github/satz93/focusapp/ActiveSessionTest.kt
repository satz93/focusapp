package io.github.satz93.focusapp

import io.github.satz93.focusapp.data.focus.ActiveSession
import io.github.satz93.focusapp.data.usage.EventType
import io.github.satz93.focusapp.data.usage.RawEvent
import io.github.satz93.focusapp.data.usage.UsageMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveSessionTest {
    private val min = 60_000L

    @Test fun countsDown() {
        val s = ActiveSession(startedAt = 0, plannedMin = 45, sessionNumber = 1)
        assertEquals(45 * min, s.remainingMs(0))
        assertEquals(35 * min, s.remainingMs(10 * min))
        assertEquals(0L, s.remainingMs(60 * min)) // never negative
        assertEquals(45 * min, s.endsAt(0))
    }

    @Test fun pauseStopsTheClockAndResumeExtendsTheEnd() {
        val s = ActiveSession(startedAt = 0, plannedMin = 25, sessionNumber = 1)
        val paused = s.paused(10 * min)
        assertTrue(paused.isPaused)
        assertEquals(15 * min, paused.remainingMs(10 * min))
        assertEquals(15 * min, paused.remainingMs(20 * min)) // frozen while paused
        val resumed = paused.resumed(20 * min)
        assertFalse(resumed.isPaused)
        assertEquals(10 * min, resumed.pausedTotalMs)
        assertEquals(15 * min, resumed.remainingMs(20 * min))
        assertEquals(35 * min, resumed.endsAt(20 * min))
        assertEquals(0L, resumed.remainingMs(35 * min))
    }

    @Test fun doublePauseOrResumeIsHarmless() {
        val s = ActiveSession(startedAt = 0, plannedMin = 25, sessionNumber = 1)
        val p = s.paused(5 * min)
        assertEquals(p, p.paused(8 * min))
        assertEquals(s, s.resumed(8 * min))
    }

    @Test fun pickupsDuringSessionOnlyCountInsideWindow() {
        val events = listOf(
            RawEvent(1_000, EventType.KEYGUARD_HIDDEN, ""), // before the session: only proves keyguard exists
            RawEvent(5_000, EventType.SCREEN_INTERACTIVE, ""), // glance at screen: not a pickup
            RawEvent(6_000, EventType.KEYGUARD_HIDDEN, ""),
            RawEvent(9_000, EventType.KEYGUARD_HIDDEN, ""),
            RawEvent(20_000, EventType.KEYGUARD_HIDDEN, ""), // after
        )
        assertEquals(2, UsageMath.countUnlocks(events, 4_000, 10_000))
    }
}
