package io.github.satz93.focusapp.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.satz93.focusapp.data.db.FocusSessionEntity
import io.github.satz93.focusapp.data.db.SessionStatus
import io.github.satz93.focusapp.ui.components.FocusCard
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.util.formatDuration
import io.github.satz93.focusapp.ui.util.formatDurationMs
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

enum class ProgressRange { DAY, WEEK }

private val AccentSoft = Color(0xFFB4BBF4)

@Composable
fun ProgressScreen(vm: ProgressViewModel) {
    var range by rememberSaveable { mutableStateOf(ProgressRange.WEEK) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Progress", style = MaterialTheme.typography.headlineMedium, color = FocusColors.Text)
            Spacer(Modifier.weight(1f))
            SegmentedToggle(range, onChange = { range = it })
        }
        Spacer(Modifier.height(12.dp))
        when (range) {
            ProgressRange.WEEK -> WeekView(vm, onOpenDay = { date ->
                vm.select(date)
                range = ProgressRange.DAY
            })
            ProgressRange.DAY -> DayView(vm)
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------- Week

@Composable
private fun WeekView(vm: ProgressViewModel, onOpenDay: (LocalDate) -> Unit) {
    val week by vm.week.collectAsStateWithLifecycle()
    if (week.days.isEmpty()) return
    val first = week.days.first().date
    val last = week.days.last().date
    val todayIndex = week.days.indexOfFirst { it.date == LocalDate.now() }.takeIf { it >= 0 }
    val labels = week.days.map { it.date.dayOfWeek.name.take(1) }

    Text(
        "${first.dayOfMonth} – ${last.format(DateTimeFormatter.ofPattern("d MMM"))}",
        style = MaterialTheme.typography.bodyMedium,
        color = FocusColors.TextSecondary,
    )
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
        StatTile("Sessions", "${week.totalCompleted} / ${week.target * 7}", Modifier.weight(1f))
        StatTile("Focus time", formatDurationMs(week.totalFocusMs), Modifier.weight(1f))
        StatTile("Avg screen", week.avgScreenMs?.let { formatDurationMs(it) } ?: "–", Modifier.weight(1f))
    }

    Spacer(Modifier.height(12.dp))
    FocusCard(Modifier.fillMaxWidth()) {
        ChartHeader("Focus sessions", "Target hit ${week.targetHitDays} of ${week.days.count { !it.isFuture }} days")
        Spacer(Modifier.height(16.dp))
        BarChart(
            values = week.days.map { if (it.isFuture) null else it.completed.toFloat() },
            labels = labels,
            reference = week.target.toFloat(),
            barColor = { _, v -> if (v >= week.target) FocusColors.Accent else AccentSoft },
            highlightIndex = todayIndex,
            onBarClick = { i -> if (!week.days[i].isFuture) onOpenDay(week.days[i].date) },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Dashed line: target of ${week.target}. Tap a day to see its sessions.",
            style = MaterialTheme.typography.bodySmall,
            color = FocusColors.TextSecondary,
        )
    }

    Spacer(Modifier.height(12.dp))
    FocusCard(Modifier.fillMaxWidth()) {
        val goalMs = week.goalMin * 60_000L
        ChartHeader(
            "Screen time",
            if (week.screenDays.isEmpty()) "No data yet" else "Under goal ${week.underGoalDays} of ${week.screenDays.size} days",
        )
        Spacer(Modifier.height(16.dp))
        BarChart(
            values = week.days.map { d -> if (d.isFuture) null else d.screenTimeMs?.let { it / 60_000f } },
            labels = labels,
            reference = week.goalMin.toFloat(),
            barColor = { _, v -> if (v * 60_000L > goalMs) FocusColors.OverLimit else FocusColors.Accent },
            highlightIndex = todayIndex,
            onBarClick = { i -> if (!week.days[i].isFuture) onOpenDay(week.days[i].date) },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Dashed line: goal of ${formatDuration(week.goalMin)}.",
            style = MaterialTheme.typography.bodySmall,
            color = FocusColors.TextSecondary,
        )
    }
}

@Composable
private fun ChartHeader(title: String, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = FocusColors.Text)
        Spacer(Modifier.weight(1f))
        Text(detail, style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    FocusCard(modifier.fillMaxHeight(), contentPadding = 12.dp) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = FocusColors.Text, maxLines = 1)
    }
}

// ---------------------------------------------------------------- Day

@Composable
private fun DayView(vm: ProgressViewModel) {
    val state by vm.day.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val stats = state.stats
    val today = LocalDate.now()

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { vm.shiftDay(-1) }) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "Previous day", tint = FocusColors.Text)
        }
        Text(
            when (selected) {
                today -> "Today"
                today.minusDays(1) -> "Yesterday"
                else -> selected.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
            },
            style = MaterialTheme.typography.titleMedium,
            color = FocusColors.Text,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { vm.shiftDay(1) }, enabled = selected < today) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Next day",
                tint = if (selected < today) FocusColors.Text else FocusColors.Border,
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    if (stats == null) return

    FocusCard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        val extra = (stats.completed - state.target).takeIf { it > 0 }?.let { " (+$it extra)" } ?: ""
        DayRow("Focus sessions", "${stats.completed} / ${state.target}$extra")
        HorizontalDivider(color = FocusColors.Border)
        DayRow("Focus time", formatDurationMs(stats.focusMs))
        HorizontalDivider(color = FocusColors.Border)
        DayRow(
            "Screen time",
            stats.screenTimeMs?.let { "${formatDurationMs(it)} of ${formatDuration(state.goalMin)}" } ?: "–",
            valueColor = if ((stats.screenTimeMs ?: 0L) > state.goalMin * 60_000L) FocusColors.OverLimit else FocusColors.Text,
        )
        HorizontalDivider(color = FocusColors.Border)
        DayRow("Pickups", stats.pickups?.toString() ?: "–")
    }
    if (stats.screenTimeMs == null && selected < today.minusDays(6)) {
        Spacer(Modifier.height(6.dp))
        Text(
            "Screen time and pickups for older days arrive once daily history is saved (milestone 7).",
            style = MaterialTheme.typography.bodySmall,
            color = FocusColors.TextSecondary,
        )
    }

    Spacer(Modifier.height(20.dp))
    Text("Session log", style = MaterialTheme.typography.titleSmall, color = FocusColors.Text)
    Spacer(Modifier.height(8.dp))
    if (stats.sessions.isEmpty()) {
        Text(
            if (selected == today) "No sessions yet today." else "No sessions on this day.",
            style = MaterialTheme.typography.bodyMedium,
            color = FocusColors.TextSecondary,
        )
    } else {
        FocusCard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
            stats.sessions.forEachIndexed { i, s ->
                if (i > 0) HorizontalDivider(color = FocusColors.Border)
                SessionRow(s)
            }
        }
    }
}

@Composable
private fun DayRow(label: String, value: String, valueColor: Color = FocusColors.Text) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = FocusColors.TextSecondary)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = valueColor)
    }
}

private val TimeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@Composable
private fun SessionRow(s: FocusSessionEntity) {
    val start = Instant.ofEpochMilli(s.startedAt).atZone(ZoneId.systemDefault()).toLocalTime()
    val (status, color) = when (s.status) {
        SessionStatus.COMPLETED -> "Completed" to FocusColors.Accent
        SessionStatus.BROKEN -> "Broken" to FocusColors.OverLimit
        else -> "Ended early" to FocusColors.Warning
    }
    val length = if (s.counts) "${s.plannedMin} min" else "${s.actualMs / 60_000} of ${s.plannedMin} min"
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(start.format(TimeFormat), style = MaterialTheme.typography.titleSmall, color = FocusColors.Text)
            Text("$status · $length", style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
        }
        Text(
            if (s.pickupsDuring == 1) "1 pickup" else "${s.pickupsDuring} pickups",
            style = MaterialTheme.typography.bodySmall,
            color = FocusColors.TextSecondary,
        )
    }
}

// ---------------------------------------------------------------- Toggle

@Composable
private fun SegmentedToggle(selected: ProgressRange, onChange: (ProgressRange) -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(Modifier.background(FocusColors.MutedFill, shape).padding(3.dp)) {
        ProgressRange.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                Modifier
                    .background(if (isSelected) FocusColors.Surface else FocusColors.MutedFill, RoundedCornerShape(10.dp))
                    .clickable(role = Role.Tab) { onChange(option) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (option == ProgressRange.DAY) "Day" else "Week",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) FocusColors.Text else FocusColors.TextSecondary,
                )
            }
        }
    }
}
