package io.github.satz93.focusapp.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.satz93.focusapp.ui.components.FocusCard
import io.github.satz93.focusapp.ui.components.LinkButton
import io.github.satz93.focusapp.ui.components.PrimaryButton
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.theme.FocusDimens
import io.github.satz93.focusapp.ui.util.formatDurationMs

@Composable
fun SessionCompleteScreen(vm: SessionCompleteViewModel, onStartNext: () -> Unit, onBackHome: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val minutes = state.session?.plannedMin ?: 0
    val done = state.doneToday
    val subtitle = when {
        done == 0 -> "You stayed focused for $minutes minutes."
        done <= state.target -> "Session $done of ${state.target} done. You stayed focused for $minutes minutes."
        else -> "Session $done done, ${done - state.target} beyond your target. You stayed focused for $minutes minutes."
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(FocusColors.Background)
            .safeDrawingPadding()
            .padding(horizontal = FocusDimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(88.dp).background(FocusColors.AccentTint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Check, contentDescription = null, tint = FocusColors.Accent, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Nice work", style = MaterialTheme.typography.headlineLarge, color = FocusColors.Text)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = FocusColors.TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
            Stat(formatDurationMs(state.focusTodayMs), "Focus today", Modifier.weight(1f))
            Stat("$done / ${state.target}", "Sessions", Modifier.weight(1f))
            Stat(state.session?.pickupsDuring?.toString() ?: "–", "Pickups during", Modifier.weight(1f))
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(text = "Start session ${done + 1}", onClick = onStartNext)
        LinkButton(text = "Back to home", onClick = onBackHome, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    FocusCard(modifier.fillMaxHeight()) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = FocusColors.Text)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = FocusColors.TextSecondary)
    }
}
