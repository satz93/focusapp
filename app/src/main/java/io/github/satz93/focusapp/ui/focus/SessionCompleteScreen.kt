package io.github.satz93.focusapp.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.ui.components.LinkButton
import io.github.satz93.focusapp.ui.components.PrimaryButton
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.theme.FocusDimens

/** Milestone 4 fills in real stats (focus today, sessions x/y, pickups during). */
@Composable
fun SessionCompleteScreen(onStartNext: () -> Unit, onBackHome: () -> Unit) {
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
        Text(
            "Session stats show up here once the timer is in.",
            style = MaterialTheme.typography.bodyMedium,
            color = FocusColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(text = "Start next session", onClick = onStartNext)
        LinkButton(text = "Back to home", onClick = onBackHome, modifier = Modifier.padding(vertical = 8.dp))
    }
}
