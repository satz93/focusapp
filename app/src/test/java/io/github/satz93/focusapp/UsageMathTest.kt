package io.github.satz93.focusapp

import io.github.satz93.focusapp.data.usage.DayPart
import io.github.satz93.focusapp.data.usage.EventType
import io.github.satz93.focusapp.data.usage.RawEvent
import io.github.satz93.focusapp.data.usage.UsageMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class UsageMathTest {
    private val zone = ZoneOffset.UTC
    private val d1 = LocalDate.of(2026, 11, 10)
    private val d2 = d1.plusDays(1)
    private val today = d1.plusDays(2)

    private fun t(date: LocalDate, h: Int, m: Int = 0) =
        LocalDateTime.of(date, java.time.LocalTime.of(h, m)).toInstant(zone).toEpochMilli()

    private fun ev(time: Long, type: Int, pkg: String = "") = RawEvent(time, type, pkg)

    @Test fun countsKeyguardHiddenAsUnlocksByDayPart() {
        val events = listOf(
            ev(t(d1, 8), EventType.SCREEN_INTERACTIVE),
            ev(t(d1, 8, 1), EventType.KEYGUARD_HIDDEN),
            ev(t(d1, 13), EventType.KEYGUARD_HIDDEN),
            ev(t(d1, 20), EventType.KEYGUARD_HIDDEN),
            ev(t(d1, 23), EventType.KEYGUARD_HIDDEN),
            ev(t(d1, 21), EventType.SCREEN_INTERACTIVE), // ignored: keyguard events exist
        )
        val day = UsageMath.aggregate(events, zone, t(d1, 23, 59), emptySet())[d1]!!
        assertEquals(4, day.unlocks)
        assertEquals(1, day.unlocksByPart[DayPart.MORNING.ordinal])
        assertEquals(1, day.unlocksByPart[DayPart.AFTERNOON.ordinal])
        assertEquals(2, day.unlocksByPart[DayPart.EVENING.ordinal])
    }

    @Test fun fallsBackToScreenOnWithoutLockScreen() {
        val events = listOf(
            ev(t(d1, 9), EventType.SCREEN_INTERACTIVE),
            ev(t(d1, 10), EventType.SCREEN_INTERACTIVE),
        )
        assertEquals(2, UsageMath.aggregate(events, zone, t(d1, 12), emptySet())[d1]!!.unlocks)
    }

    @Test fun appTimeFromResumePauseAndScreenOff() {
        val events = listOf(
            ev(t(d1, 9, 0), EventType.ACTIVITY_RESUMED, "insta"),
            ev(t(d1, 9, 10), EventType.ACTIVITY_PAUSED, "insta"),
            ev(t(d1, 9, 10), EventType.ACTIVITY_RESUMED, "yt"),
            ev(t(d1, 9, 40), EventType.SCREEN_NON_INTERACTIVE),
            ev(t(d1, 9, 50), EventType.ACTIVITY_PAUSED, "yt"), // already closed by screen off
            ev(t(d1, 10, 0), EventType.ACTIVITY_RESUMED, "launcher"),
            ev(t(d1, 10, 5), EventType.ACTIVITY_PAUSED, "launcher"),
        )
        val day = UsageMath.aggregate(events, zone, t(d1, 12), setOf("launcher"))[d1]!!
        assertEquals(10 * 60_000L, day.appMs["insta"])
        assertEquals(30 * 60_000L, day.appMs["yt"])
        assertFalse("launcher" in day.appMs)
        assertEquals(40 * 60_000L, day.screenTimeMs)
    }

    @Test fun sessionAcrossMidnightIsSplit() {
        val events = listOf(
            ev(t(d1, 23, 50), EventType.ACTIVITY_RESUMED, "yt"),
            ev(t(d2, 0, 15), EventType.ACTIVITY_PAUSED, "yt"),
        )
        val days = UsageMath.aggregate(events, zone, t(d2, 12), emptySet())
        assertEquals(10 * 60_000L, days[d1]!!.appMs["yt"])
        assertEquals(15 * 60_000L, days[d2]!!.appMs["yt"])
    }

    @Test fun openSessionClosedAtEndTime() {
        val events = listOf(ev(t(today, 9), EventType.ACTIVITY_RESUMED, "yt"))
        val day = UsageMath.aggregate(events, zone, t(today, 9, 20), emptySet())[today]!!
        assertEquals(20 * 60_000L, day.appMs["yt"])
    }

    @Test fun summaryAveragesFullDaysOnly() {
        val events = listOf(
            ev(t(d1, 9), EventType.KEYGUARD_HIDDEN),
            ev(t(d1, 9), EventType.ACTIVITY_RESUMED, "yt"),
            ev(t(d1, 10), EventType.ACTIVITY_PAUSED, "yt"),
            ev(t(d2, 9), EventType.KEYGUARD_HIDDEN),
            ev(t(d2, 14), EventType.KEYGUARD_HIDDEN),
            ev(t(d2, 15), EventType.KEYGUARD_HIDDEN),
            ev(t(today, 9), EventType.KEYGUARD_HIDDEN), // today is excluded from the average
        )
        val days = UsageMath.aggregate(events, zone, t(today, 10), emptySet())
        val s = UsageMath.summarize(days, today, unlocksAvailable = true)
        assertEquals(2, s.daysCounted)
        assertFalse(s.isPartialDay)
        assertEquals(2, s.avgUnlocks)
        assertEquals(30 * 60_000L, s.avgScreenTimeMs)
        assertEquals("yt", s.avgAppMs.first().first)
    }

    @Test fun summaryFallsBackToTodayWhenNoHistory() {
        val events = listOf(ev(t(today, 9), EventType.KEYGUARD_HIDDEN))
        val days = UsageMath.aggregate(events, zone, t(today, 10), emptySet())
        val s = UsageMath.summarize(days, today, unlocksAvailable = true)
        assertTrue(s.isPartialDay)
        assertEquals(1, s.avgUnlocks)
    }

    @Test fun minutesBetweenUnlocks() {
        assertEquals(10, UsageMath.minutesBetweenUnlocks(96, 16))
        assertEquals(null, UsageMath.minutesBetweenUnlocks(0, 16))
    }
}
