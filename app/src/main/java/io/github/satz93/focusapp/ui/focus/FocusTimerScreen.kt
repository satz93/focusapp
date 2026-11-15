package io.github.satz93.focusapp.ui.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.ui.components.LinkButton
import io.github.satz93.focusapp.ui.components.PrimaryButton
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.theme.FocusDimens

/** Dark focus timer. Milestone 4 adds the real countdown, foreground service and session logging. */
@Composable
fun FocusTimerScreen(onClose: () -> Unit, onComplete: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(FocusColors.DarkBackground)
            .safeDrawingPadding()
            .padding(horizontal = FocusDimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = FocusColors.OnDark)
            }
        }
        Spacer(Modifier.height(32.dp))
        Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = FocusColors.AccentOnDark,
                    style = Stroke(width = 6.dp.toPx()),
                    radius = size.minDimension / 2 - 3.dp.toPx(),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("45:00", style = MaterialTheme.typography.displayMedium, color = FocusColors.OnDark)
                Text("Ready when you are", style = MaterialTheme.typography.bodySmall, color = FocusColors.OnDarkSecondary)
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Distracting apps are locked until the timer ends. Put your phone face down.",
            style = MaterialTheme.typography.bodyMedium,
            color = FocusColors.OnDarkSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Text(
            "Timer arrives in milestone 4. Tap below to preview the next screen.",
            style = MaterialTheme.typography.bodySmall,
            color = FocusColors.OnDarkSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        PrimaryButton(text = "Start focus", onClick = onComplete, onDark = true)
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(vertical = 8.dp)) {
            LinkButton(text = "End session early", onClick = onClose, color = FocusColors.AccentOnDark)
        }
    }
}
