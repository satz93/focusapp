package io.github.satz93.focusapp.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.ui.components.FocusCard
import io.github.satz93.focusapp.ui.theme.FocusColors

enum class ProgressRange { DAY, WEEK }

/** Milestone 7 adds Room history and the Day list / Week charts. */
@Composable
fun ProgressScreen() {
    var range by rememberSaveable { mutableStateOf(ProgressRange.WEEK) }
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Progress", style = MaterialTheme.typography.headlineMedium, color = FocusColors.Text)
            Spacer(Modifier.weight(1f))
            SegmentedToggle(range, onChange = { range = it })
        }
        Spacer(Modifier.height(20.dp))
        FocusCard(Modifier.fillMaxWidth()) {
            Text(
                if (range == ProgressRange.WEEK) "Weekly charts" else "Today's summary",
                style = MaterialTheme.typography.titleSmall,
                color = FocusColors.Text,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "History builds up as you use the app. Charts arrive in milestone 7.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun SegmentedToggle(selected: ProgressRange, onChange: (ProgressRange) -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(Modifier.background(FocusColors.MutedFill, shape).padding(3.dp)) {
        ProgressRange.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                Modifier
                    .background(if (isSelected) FocusColors.Surface else FocusColors.MutedFill, RoundedCornerShape(10.dp))
                    .clickable(role = Role.Tab) { onChange(option) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (option == ProgressRange.DAY) "Day" else "Week",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) FocusColors.Text else FocusColors.TextSecondary,
                )
            }
        }
    }
}
