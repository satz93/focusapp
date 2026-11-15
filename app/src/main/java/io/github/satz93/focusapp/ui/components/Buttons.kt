package io.github.satz93.focusapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.theme.FocusDimens

private val ButtonShape = RoundedCornerShape(FocusDimens.ButtonRadius)

/** Near-black, 56dp tall. On dark screens pass [onDark] = true for the white variant. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDark: Boolean = false,
) {
    val container = if (onDark) FocusColors.Surface else FocusColors.Text
    val content = if (onDark) FocusColors.Text else FocusColors.Surface
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(FocusDimens.ButtonHeight),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = if (onDark) FocusColors.DarkCard else FocusColors.MutedFill,
            disabledContentColor = if (onDark) FocusColors.OnDarkSecondary else FocusColors.TextSecondary,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** White with a hairline border (e.g. "Close app" on the limit sheet). */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDark: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(FocusDimens.ButtonHeight),
        shape = ButtonShape,
        border = BorderStroke(1.dp, if (onDark) FocusColors.DarkLine else FocusColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (onDark) Color.Transparent else FocusColors.Surface,
            contentColor = if (onDark) FocusColors.OnDark else FocusColors.Text,
            disabledContentColor = if (onDark) FocusColors.OnDarkSecondary else FocusColors.TextSecondary,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Small text-only action such as "End session early" or "Back to home". */
@Composable
fun LinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = FocusColors.Accent,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = FocusDimens.MinTouch),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

