package io.github.satz93.focusapp.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import io.github.satz93.focusapp.data.apps.AppInfoProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class UsageRepository(
    context: Context,
    private val apps: AppInfoProvider,
) {
    private val appContext = context.applicationContext
    private val usm = appContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun hasUsageAccess(): Boolean {
        val appOps = appContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), appContext.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), appContext.packageName)
        }
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            appContext.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    /** Unlock / screen-on events only exist from Android 9. */
    val unlocksAvailable: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    /**
     * Averages over the last [daysBack] full days. Android only keeps detailed events for
     * about a week, which is why the app will persist its own history (milestone 7).
     */
    suspend fun loadSummary(daysBack: Long = 7): UsageSummary = withContext(Dispatchers.Default) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val begin = today.minusDays(daysBack).atStartOfDay(zone).toInstant().toEpochMilli()
        val days = aggregate(begin, System.currentTimeMillis(), zone)
        UsageMath.summarize(days, today, unlocksAvailable)
    }

    /** Today so far. */
    suspend fun loadToday(): DayUsage = withContext(Dispatchers.Default) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val begin = today.atStartOfDay(zone).toInstant().toEpochMilli()
        aggregate(begin, System.currentTimeMillis(), zone)[today] ?: DayUsage(today)
    }

    /** Per-day usage for an inclusive date range (only as far back as Android keeps events). */
    suspend fun loadDays(from: LocalDate, to: LocalDate): Map<LocalDate, DayUsage> = withContext(Dispatchers.Default) {
        val zone = ZoneId.systemDefault()
        val begin = from.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = minOf(System.currentTimeMillis(), to.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli())
        if (end <= begin) emptyMap() else aggregate(begin, end, zone)
    }

    /**
     * Pickups between two moments (e.g. during a focus session). Looks back a day before [from]
     * only to decide whether this phone reports lock-screen events.
     */
    suspend fun countUnlocks(from: Long, to: Long): Int = withContext(Dispatchers.Default) {
        if (!hasUsageAccess() || !unlocksAvailable || to <= from) return@withContext 0
        UsageMath.countUnlocks(readEvents(from - 24 * 60 * 60_000L, to), from, to)
    }

    private fun aggregate(begin: Long, end: Long, zone: ZoneId): Map<LocalDate, DayUsage> {
        if (!hasUsageAccess()) return emptyMap()
        val events = readEvents(begin, end)
        return UsageMath.aggregate(events, zone, end, excludedPackages())
    }

    private fun readEvents(begin: Long, end: Long): List<RawEvent> {
        val out = ArrayList<RawEvent>(4096)
        val result = usm.queryEvents(begin, end) ?: return out
        val e = UsageEvents.Event()
        while (result.hasNextEvent()) {
            result.getNextEvent(e)
            if (e.eventType in EventType.relevant) {
                out += RawEvent(e.timeStamp, e.eventType, e.packageName.orEmpty())
            }
        }
        return out
    }

    /** Not "phone time" in the sense people mean: home screen, system UI, and this app itself. */
    private fun excludedPackages(): Set<String> =
        apps.launcherPackages() + setOf(appContext.packageName, "com.android.systemui", "android")
}
