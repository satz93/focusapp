package io.github.satz93.focusapp.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.R
import io.github.satz93.focusapp.ui.components.PrimaryButton
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.theme.FocusDimens
import io.github.satz93.focusapp.ui.theme.FocusTheme
import io.github.satz93.focusapp.ui.theme.SpaceGrotesk

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(FocusColors.Background)
            .safeDrawingPadding()
            .padding(horizontal = FocusDimens.ScreenPadding),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            fontFamily = SpaceGrotesk,
            color = FocusColors.Text,
        )
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(200.dp).background(FocusColors.AccentTint, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.PhoneAndroid,
                    contentDescription = null,
                    tint = FocusColors.Accent,
                    modifier = Modifier.size(80.dp),
                )
            }
        }
        Text(
            "Pick up your phone less. Do more of what matters.",
            style = MaterialTheme.typography.headlineLarge,
            color = FocusColors.Text,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "See your real phone habits, set gentle limits, and build focus one session at a time.",
            style = MaterialTheme.typography.bodyMedium,
            color = FocusColors.TextSecondary,
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(text = "Get started", onClick = onGetStarted)
        Spacer(Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAF7, widthDp = 390, heightDp = 845)
@Composable
private fun WelcomePreview() = FocusTheme { WelcomeScreen(onGetStarted = {}) }
