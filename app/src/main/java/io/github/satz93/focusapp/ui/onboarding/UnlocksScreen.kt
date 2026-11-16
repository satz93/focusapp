package io.github.satz93.focusapp.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.satz93.focusapp.data.usage.DayPart
import io.github.satz93.focusapp.ui.components.FocusCard
import io.github.satz93.focusapp.ui.components.OnboardingScaffold
import io.github.satz93.focusapp.ui.theme.FocusColors

@Composable
fun UnlocksScreen(vm: OnboardingViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    LaunchedEffect(Unit) { vm.loadUsageIfNeeded() }
    val state by vm.usageState.collectAsStateWithLifecycle()

    OnboardingScaffold(step = 1, onBack = onBack, primaryText = "Next", onPrimary = onNext) {
        when (val s = state) {
            is UsageState.Ready -> UnlocksContent(s.insights)
            else -> UsageLoading()
        }
    }
}

@Composable
private fun UnlocksContent(insights: UsageInsights) {
    val summary = insights.summary
    Text(
        "YOU UNLOCK YOUR PHONE",
        style = MaterialTheme.typography.labelSmall,
        color = FocusColors.TextSecondary,
    )
    Spacer(Modifier.height(8.dp))

    if (!summary.unlocksAvailable) {
        Text(
            "Unlock counts need Android 9 or newer, so they can’t be shown on this phone. Everything else still works.",
            style = MaterialTheme.typography.bodyLarge,
            color = FocusColors.Text,
        )
        return
    }
    if (summary.avgUnlocks == 0) {
        Text(
            "No unlocks recorded yet. Your phone hasn’t kept enough history, so the app will start counting from today.",
            style = MaterialTheme.typography.bodyLarge,
            color = FocusColors.Text,
        )
        return
    }

    Row(verticalAlignment = Alignment.Bottom) {
        Text("${summary.avgUnlocks}", style = MaterialTheme.typography.displayLarge, color = FocusColors.Text)
        Spacer(Modifier.width(10.dp))
        Text(
            if (summary.isPartialDay) "times so far today" else "times a day",
            style = MaterialTheme.typography.titleMedium,
            color = FocusColors.Text,
            modifier = Modifier.padding(bottom = 10.dp),
        )
    }
    insights.minutesBetweenUnlocks?.let { mins ->
        Spacer(Modifier.height(8.dp))
        Text(
            buildAnnotatedString {
                append("That’s once every ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(if (mins == 1) "minute" else "$mins minutes")
                }
                append(" you’re awake.")
            },
            style = MaterialTheme.typography.bodyLarge,
            color = FocusColors.Text,
        )
    }
    if (!summary.isPartialDay && summary.daysCounted > 0) {
        Spacer(Modifier.height(4.dp))
        Text(
            "Average of the last ${summary.daysCounted} ${if (summary.daysCounted == 1) "day" else "days"}.",
            style = MaterialTheme.typography.bodySmall,
            color = FocusColors.TextSecondary,
        )
    }

    Spacer(Modifier.height(24.dp))
    FocusCard(Modifier.fillMaxWidth()) {
        Text("When you pick it up", style = MaterialTheme.typography.titleSmall, color = FocusColors.Text)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DayPart.entries.forEach { part ->
                PartTile(
                    label = part.label(),
                    value = summary.avgUnlocksByPart[part.ordinal],
                    highlighted = part == insights.busiestPart,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        insights.busiestPart?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                "${it.plural()} are your busiest time for pickups.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun PartTile(label: String, value: Int, highlighted: Boolean, modifier: Modifier = Modifier) {
    val bg = if (highlighted) FocusColors.AccentTint else FocusColors.MutedFill
    val fg = if (highlighted) FocusColors.Accent else FocusColors.Text
    Column(
        modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = if (highlighted) FocusColors.Accent else FocusColors.TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text("$value", style = MaterialTheme.typography.titleLarge, color = fg)
    }
}

private fun DayPart.label() = when (this) {
    DayPart.MORNING -> "Morning"
    DayPart.AFTERNOON -> "Afternoon"
    DayPart.EVENING -> "Evening"
}

private fun DayPart.plural() = when (this) {
    DayPart.MORNING -> "Mornings"
    DayPart.AFTERNOON -> "Afternoons"
    DayPart.EVENING -> "Evenings"
}
