package io.github.satz93.focusapp.data.usage

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Pure aggregation of usage events, with no Android types, so it can be unit tested.
 *
 * Unlock counting: we count `KEYGUARD_HIDDEN` events (the lock screen went away = the user
 * got into the phone). That matches "pickups" closely and ignores the screen lighting up for
 * notifications. If a phone has no lock screen at all ("None"), there are no keyguard events,
 * so we fall back to counting `SCREEN_INTERACTIVE` (screen turned on). Both event types exist
 * from Android 9 (API 28); on Android 8 unlocks can't be counted and show as unavailable.
 *
 * App time: built from `ACTIVITY_RESUMED` / `ACTIVITY_PAUSED` / `ACTIVITY_STOPPED` pairs, closed early when
 * the screen turns off or locks. This matches what Digital Wellbeing reports more closely than
 * the daily usage-stats buckets, which straddle day boundaries.
 */
object EventType {
    // Values mirror android.app.usage.UsageEvents.Event constants.
    const val ACTIVITY_RESUMED = 1
    const val ACTIVITY_PAUSED = 2
    const val SCREEN_INTERACTIVE = 15
    const val SCREEN_NON_INTERACTIVE = 16
    const val KEYGUARD_SHOWN = 17
    const val KEYGUARD_HIDDEN = 18
    const val ACTIVITY_STOPPED = 23
    const val DEVICE_SHUTDOWN = 26

    val relevant = setOf(
        ACTIVITY_RESUMED, ACTIVITY_PAUSED, ACTIVITY_STOPPED, SCREEN_INTERACTIVE,
        SCREEN_NON_INTERACTIVE, KEYGUARD_SHOWN, KEYGUARD_HIDDEN, DEVICE_SHUTDOWN,
    )
}

data class RawEvent(val time: Long, val type: Int, val pkg: String)

enum class DayPart { MORNING, AFTERNOON, EVENING }

/** Morning 05:00–11:59, afternoon 12:00–16:59, evening 17:00–04:59 (late night counts as evening). */
fun dayPartOf(hour: Int): DayPart = when (hour) {
    in 5..11 -> DayPart.MORNING
    in 12..16 -> DayPart.AFTERNOON
    else -> DayPart.EVENING
}

class DayUsage(val date: LocalDate) {
    var unlocks: Int = 0
    val unlocksByPart: IntArray = IntArray(DayPart.entries.size)
    val appMs: MutableMap<String, Long> = mutableMapOf()
    var hadEvents: Boolean = false

    val screenTimeMs: Long get() = appMs.values.sum()
}

data class UsageSummary(
    /** Number of days the averages are based on. 0 = no data at all. */
    val daysCounted: Int,
    /** True when there were no full past days and numbers are from today so far. */
    val isPartialDay: Boolean,
    val unlocksAvailable: Boolean,
    val avgUnlocks: Int,
    val avgUnlocksByPart: List<Int>,
    val avgScreenTimeMs: Long,
    /** Average ms per day for each app, highest first. */
    val avgAppMs: List<Pair<String, Long>>,
)

object UsageMath {

    fun aggregate(
        events: List<RawEvent>,
        zone: ZoneId,
        endTime: Long,
        excludedPackages: Set<String>,
    ): Map<LocalDate, DayUsage> {
        val days = sortedMapOf<LocalDate, DayUsage>()
        fun day(date: LocalDate) = days.getOrPut(date) { DayUsage(date) }

        val sorted = events.sortedBy { it.time }
        val useKeyguard = sorted.any { it.type == EventType.KEYGUARD_HIDDEN }
        val unlockType = if (useKeyguard) EventType.KEYGUARD_HIDDEN else EventType.SCREEN_INTERACTIVE

        var currentPkg: String? = null
        var currentStart = 0L

        fun addInterval(pkg: String, from: Long, to: Long) {
            if (pkg in excludedPackages || to <= from) return
            var start = from
            while (start < to) {
                val date = Instant.ofEpochMilli(start).atZone(zone).toLocalDate()
                val nextDay = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = minOf(to, nextDay)
                val d = day(date)
                d.appMs[pkg] = (d.appMs[pkg] ?: 0L) + (end - start)
                start = end
            }
        }

        fun close(at: Long) {
            currentPkg?.let { addInterval(it, currentStart, at) }
            currentPkg = null
        }

        for (e in sorted) {
            val zoned = Instant.ofEpochMilli(e.time).atZone(zone)
            day(zoned.toLocalDate()).hadEvents = true
            if (e.type == unlockType) {
                val d = day(zoned.toLocalDate())
                d.unlocks++
                d.unlocksByPart[dayPartOf(zoned.hour).ordinal]++
            }
            when (e.type) {
                EventType.ACTIVITY_RESUMED -> {
                    if (currentPkg != e.pkg) {
                        close(e.time)
                        currentPkg = e.pkg
                        currentStart = e.time
                    }
                }
                EventType.ACTIVITY_PAUSED, EventType.ACTIVITY_STOPPED -> {
                    if (currentPkg == e.pkg) close(e.time)
                }
                EventType.SCREEN_NON_INTERACTIVE, EventType.KEYGUARD_SHOWN, EventType.DEVICE_SHUTDOWN -> close(e.time)
            }
        }
        close(endTime)
        return days
    }

    /**
     * Averages over full past days that have data. If there are none yet (fresh phone or the
     * system kept no history), falls back to today so far.
     */
    fun summarize(
        days: Map<LocalDate, DayUsage>,
        today: LocalDate,
        unlocksAvailable: Boolean,
    ): UsageSummary {
        val fullDays = days.values.filter { it.date < today && it.hadEvents }
        val partial = fullDays.isEmpty()
        val basis = if (partial) listOfNotNull(days[today]) else fullDays
        val n = basis.size
        if (n == 0) {
            return UsageSummary(0, true, unlocksAvailable, 0, List(DayPart.entries.size) { 0 }, 0L, emptyList())
        }
        val apps = mutableMapOf<String, Long>()
        basis.forEach { d -> d.appMs.forEach { (pkg, ms) -> apps[pkg] = (apps[pkg] ?: 0L) + ms } }
        return UsageSummary(
            daysCounted = n,
            isPartialDay = partial,
            unlocksAvailable = unlocksAvailable,
            avgUnlocks = roundDiv(basis.sumOf { it.unlocks }.toLong(), n).toInt(),
            avgUnlocksByPart = DayPart.entries.map { p ->
                roundDiv(basis.sumOf { it.unlocksByPart[p.ordinal] }.toLong(), n).toInt()
            },
            avgScreenTimeMs = basis.sumOf { it.screenTimeMs } / n,
            avgAppMs = apps.map { (pkg, ms) -> pkg to ms / n }.sortedByDescending { it.second },
        )
    }

    /** Unlocks inside [from, to], using the same keyguard-or-screen-on rule as [aggregate]. */
    fun countUnlocks(events: List<RawEvent>, from: Long, to: Long): Int {
        val type = if (events.any { it.type == EventType.KEYGUARD_HIDDEN }) EventType.KEYGUARD_HIDDEN
        else EventType.SCREEN_INTERACTIVE
        return events.count { it.type == type && it.time in from..to }
    }

    /** "That's once every X minutes you're awake." Null when there are no unlocks. */
    fun minutesBetweenUnlocks(avgUnlocks: Int, wakingHours: Int): Int? =
        if (avgUnlocks <= 0) null else maxOf(1, Math.round(wakingHours * 60f / avgUnlocks))

    private fun roundDiv(total: Long, n: Int): Long = Math.round(total.toDouble() / n)
}
