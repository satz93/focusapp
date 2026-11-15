package io.github.satz93.focusapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.theme.FocusDimens

const val ONBOARDING_STEPS = 6

/**
 * Shared layout for onboarding steps 2–7: back arrow + dot indicator on top,
 * scrollable content, and the primary action pinned to the bottom.
 */
@Composable
fun OnboardingScaffold(
    step: Int,
    onBack: () -> Unit,
    primaryText: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = FocusDimens.ScreenPadding),
    ) {
        Box(Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(0.dp)) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = FocusColors.Text,
                    modifier = Modifier.size(22.dp),
                )
            }
            StepIndicator(current = step, total = ONBOARDING_STEPS, modifier = Modifier.align(Alignment.Center))
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp),
            content = content,
        )
        Spacer(Modifier.height(12.dp))
        PrimaryButton(text = primaryText, onClick = onPrimary, enabled = primaryEnabled)
        Spacer(Modifier.height(16.dp))
    }
}
