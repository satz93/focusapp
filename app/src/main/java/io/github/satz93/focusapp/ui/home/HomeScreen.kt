package io.github.satz93.focusapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.satz93.focusapp.ui.components.FocusCard
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.util.formatDuration
import io.github.satz93.focusapp.ui.util.formatDurationMs
import io.github.satz93.focusapp.data.settings.GuardConfig
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import io.github.satz93.focusapp.ui.util.formatClock
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(vm: HomeViewModel, onStartSession: () -> Unit, onShowComplete: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val pendingComplete by vm.pendingComplete.collectAsStateWithLifecycle()
    LaunchedEffect(pendingComplete) { if (pendingComplete != null) onShowComplete() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
    val now = remember { LocalDateTime.now() }
    val greeting = when (now.hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            now.format(DateTimeFormatter.ofPattern("EEEE, d MMM")),
            style = MaterialTheme.typography.bodyMedium,
            color = FocusColors.TextSecondary,
        )
        Text(greeting, style = MaterialTheme.typography.headlineMedium, color = FocusColors.Text)
        Spacer(Modifier.height(20.dp))

        FocusHeroCard(state, onStartSession)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
            ScreenTimeCard(state, Modifier.weight(1f).fillMaxHeight())
            PickupsCard(state, Modifier.weight(1f).fillMaxHeight())
        }
        if (!state.hasUsageAccess) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Usage access is off, so screen time and pickups can\u2019t be shown.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusColors.OverLimit,
            )
        }
        Spacer(Modifier.height(12.dp))
        FocusCard(Modifier.fillMaxWidth()) {
            Text("Distracting apps", style = MaterialTheme.typography.titleSmall, color = FocusColors.Text)
            Spacer(Modifier.height(6.dp))
            Text(
                "Your app limits show up here once usage tracking is in (milestone 4).",
                style = MaterialTheme.typography.bodySmall,
                color = FocusColors.TextSecondary,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ScreenTimeCard(state: HomeUiState, modifier: Modifier = Modifier) {
    val goalMs = state.screenTimeGoalMin * 60_000L
    val usedMs = state.today?.screenTimeMs
    FocusCard(modifier) {
        Text("Screen time", style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
        Spacer(Modifier.height(6.dp))
        Text(
            usedMs?.let { formatDurationMs(it) } ?: "\u2013",
            style = MaterialTheme.typography.titleLarge,
            color = FocusColors.Text,
        )
        Spacer(Modifier.height(8.dp))
        val fraction = if (usedMs == null || goalMs <= 0) 0f else usedMs.toFloat() / goalMs
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = FocusColors.usage(fraction, GuardConfig.HOME_WARN_FRACTION),
            trackColor = FocusColors.MutedFill,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "of ${formatDuration(state.screenTimeGoalMin)} goal",
            style = MaterialTheme.typography.bodySmall,
            color = FocusColors.TextSecondary,
        )
    }
}

@Composable
private fun PickupsCard(state: HomeUiState, modifier: Modifier = Modifier) {
    val today = state.today
    FocusCard(modifier) {
        Text("Pickups", style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
        Spacer(Modifier.height(6.dp))
        Text(
            if (today != null && today.unlocksAvailable) "${today.pickups}" else "\u2013",
            style = MaterialTheme.typography.titleLarge,
            color = FocusColors.Text,
        )
        Spacer(Modifier.height(4.dp))
        Text("so far today", style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
        today?.avgPickups?.let {
            Text("avg $it / day", style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
        }
    }
}

@Composable
private fun FocusHeroCard(state: HomeUiState, onStartSession: () -> Unit) {
    val active = state.active
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(active) {
        while (active != null && !active.isPaused) {
            now = System.currentTimeMillis()
            delay(500)
        }
        now = System.currentTimeMillis()
    }

    FocusCard(
        modifier = Modifier.fillMaxWidth(),
        color = FocusColors.Text,
        borderColor = null,
        contentPadding = 16.dp,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (active != null) "Focus session in progress" else "Focus sessions today",
                style = MaterialTheme.typography.bodySmall,
                color = FocusColors.OnDark,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${active?.plannedMin ?: state.sessionLengthMin} min" + if (active == null) " each" else "",
                style = MaterialTheme.typography.bodySmall,
                color = FocusColors.OnDarkSecondary,
            )
        }
        Spacer(Modifier.height(12.dp))
        if (active != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    formatClock((active.remainingMs(now) + 999) / 1000),
                    style = MaterialTheme.typography.displaySmall,
                    color = FocusColors.OnDark,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (active.isPaused) "paused" else "left",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusColors.OnDarkSecondary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        } else {
            SessionsDoneRow(state)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onStartSession,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = FocusColors.Surface,
                contentColor = FocusColors.Text,
            ),
        ) {
            Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (active != null) "Back to session" else "Start session ${state.sessionsDone + 1}",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/** "2 of 3 done" with dots. Sessions beyond the target add extra dots; there's no cap. */
@Composable
private fun SessionsDoneRow(state: HomeUiState) {
    val done = state.sessionsDone
    val target = state.sessionsTarget
    Row(verticalAlignment = Alignment.Bottom) {
        Text("$done", style = MaterialTheme.typography.displaySmall, color = FocusColors.OnDark)
        Spacer(Modifier.width(6.dp))
        Column(Modifier.padding(bottom = 4.dp)) {
            if (done >= target) {
                Text(
                    if (done == target) "Target hit" else "Target hit \u00b7 ${done - target} extra",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusColors.AccentOnDark,
                )
            }
            Text("of $target done", style = MaterialTheme.typography.bodyMedium, color = FocusColors.OnDarkSecondary)
        }
        Spacer(Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 6.dp, start = 8.dp),
        ) {
            val dots = maxOf(done, target)
            val shown = minOf(dots, 8)
            repeat(shown) { i ->
                Box(
                    Modifier
                        .size(16.dp)
                        .then(
                            if (i < done) Modifier.background(FocusColors.AccentOnDark, CircleShape)
                            else Modifier.border(1.5.dp, FocusColors.OnDarkSecondary, CircleShape),
                        ),
                )
            }
            if (dots > shown) {
                Text("+${dots - shown}", style = MaterialTheme.typography.bodySmall, color = FocusColors.AccentOnDark)
            }
        }
    }
    if (state.focusTodayMs > 0) {
        Spacer(Modifier.height(6.dp))
        Text(
            "${formatDurationMs(state.focusTodayMs)} focused today",
            style = MaterialTheme.typography.bodySmall,
            color = FocusColors.OnDarkSecondary,
        )
    }
}
