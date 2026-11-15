package io.github.satz93.focusapp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.ui.theme.FocusColors

/** Onboarding progress: current step is a stretched pill, the others small dots. [current] is 0-based. */
@Composable
fun StepIndicator(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics { contentDescription = "Step ${current + 1} of $total" },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val active = i == current
            val width by animateDpAsState(if (active) 22.dp else 6.dp, label = "dotWidth")
            val color by animateColorAsState(
                if (active) FocusColors.Text else FocusColors.Border,
                label = "dotColor",
            )
            Box(
                Modifier
                    .width(width)
                    .height(6.dp)
                    .background(color, CircleShape),
            )
        }
    }
}
