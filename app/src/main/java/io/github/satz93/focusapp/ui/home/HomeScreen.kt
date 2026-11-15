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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(vm: HomeViewModel, onStartSession: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
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

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FocusCard(Modifier.weight(1f)) {
                Text("Screen time", style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text("–", style = MaterialTheme.typography.titleLarge, color = FocusColors.Text)
                Text(
                    "of ${formatDuration(state.screenTimeGoalMin)} goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusColors.TextSecondary,
                )
            }
            FocusCard(Modifier.weight(1f)) {
                Text("Pickups", style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text("–", style = MaterialTheme.typography.titleLarge, color = FocusColors.Text)
                Text("so far today", style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
            }
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
private fun FocusHeroCard(state: HomeUiState, onStartSession: () -> Unit) {
    FocusCard(
        modifier = Modifier.fillMaxWidth(),
        color = FocusColors.Text,
        borderColor = null,
        contentPadding = 16.dp,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Focus sessions today", style = MaterialTheme.typography.bodySmall, color = FocusColors.OnDark)
            Spacer(Modifier.weight(1f))
            Text(
                "${state.sessionLengthMin} min each",
                style = MaterialTheme.typography.bodySmall,
                color = FocusColors.OnDarkSecondary,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("${state.sessionsDone}", style = MaterialTheme.typography.displaySmall, color = FocusColors.OnDark)
            Spacer(Modifier.width(6.dp))
            Text(
                "of ${state.sessionsTarget} done",
                style = MaterialTheme.typography.bodyMedium,
                color = FocusColors.OnDarkSecondary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                repeat(state.sessionsTarget) { i ->
                    val done = i < state.sessionsDone
                    Box(
                        Modifier
                            .size(18.dp)
                            .then(
                                if (done) Modifier.background(FocusColors.AccentOnDark, CircleShape)
                                else Modifier.border(1.5.dp, FocusColors.OnDarkSecondary, CircleShape),
                            ),
                    )
                }
            }
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
            Text("Start session ${state.sessionsDone + 1}", style = MaterialTheme.typography.labelLarge)
        }
    }
}

