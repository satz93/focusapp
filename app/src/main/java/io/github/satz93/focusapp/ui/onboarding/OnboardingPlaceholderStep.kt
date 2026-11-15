package io.github.satz93.focusapp.ui.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.ui.components.OnboardingScaffold
import io.github.satz93.focusapp.ui.theme.FocusColors

/** Temporary stand-in so the whole flow is clickable before each step is built. */
@Composable
fun OnboardingPlaceholderStep(
    step: Int,
    title: String,
    milestone: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    primaryText: String = "Next",
) {
    OnboardingScaffold(step = step, onBack = onBack, primaryText = primaryText, onPrimary = onNext) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = FocusColors.Text)
        Spacer(Modifier.height(12.dp))
        Text(
            "This step is built in milestone $milestone.",
            style = MaterialTheme.typography.bodyMedium,
            color = FocusColors.TextSecondary,
        )
    }
}
