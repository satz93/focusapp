package io.github.satz93.focusapp.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.satz93.focusapp.ui.components.AppIcon
import io.github.satz93.focusapp.ui.components.FocusCard
import io.github.satz93.focusapp.ui.components.OnboardingScaffold
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.theme.SpaceGrotesk
import io.github.satz93.focusapp.ui.util.formatDurationMs
import kotlin.math.roundToInt

@Composable
fun TopAppsScreen(vm: OnboardingViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    LaunchedEffect(Unit) { vm.loadUsageIfNeeded() }
    val state by vm.usageState.collectAsStateWithLifecycle()

    OnboardingScaffold(step = 2, onBack = onBack, primaryText = "Next", onPrimary = onNext) {
        Text("Where your time goes", style = MaterialTheme.typography.headlineMedium, color = FocusColors.Text)
        Spacer(Modifier.height(10.dp))
        when (val s = state) {
            is UsageState.Ready -> TopAppsContent(s.insights)
            else -> UsageLoading()
        }
    }
}

@Composable
private fun TopAppsContent(insights: UsageInsights) {
    if (insights.topApps.isEmpty()) {
        Text(
            "Not enough app history yet. Use your phone as normal and your top apps will show up on Home.",
            style = MaterialTheme.typography.bodyMedium,
            color = FocusColors.TextSecondary,
        )
        return
    }
    val count = insights.topApps.size
    Text(
        buildAnnotatedString {
            append(if (count == 1) "Your top app takes " else "Your top $count apps take ")
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = FocusColors.Text)) {
                append("${(insights.topShare * 100).roundToInt()}%")
            }
            append(" of your phone time")
            append(if (insights.summary.isPartialDay) " today." else ".")
        },
        style = MaterialTheme.typography.bodyMedium,
        color = FocusColors.TextSecondary,
    )
    Spacer(Modifier.height(20.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        insights.topApps.forEach { app -> TopAppCard(app) }
    }
    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Text("All other apps", style = MaterialTheme.typography.bodyMedium, color = FocusColors.TextSecondary)
        Spacer(Modifier.weight(1f))
        Text(
            formatDurationMs(insights.otherAppsMs),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = FocusColors.TextSecondary,
        )
    }
}

@Composable
private fun TopAppCard(app: TopApp) {
    FocusCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(pkg = app.pkg, label = app.label)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(app.label, style = MaterialTheme.typography.titleSmall, color = FocusColors.Text, maxLines = 1)
                Text(
                    "${(app.share * 100).roundToInt()}% of your day",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusColors.TextSecondary,
                )
            }
            Text(
                formatDurationMs(app.avgMs),
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = SpaceGrotesk),
                color = FocusColors.Text,
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { app.share.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = FocusColors.Accent,
            trackColor = FocusColors.MutedFill,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}
