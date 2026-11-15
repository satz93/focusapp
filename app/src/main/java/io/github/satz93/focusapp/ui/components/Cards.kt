package io.github.satz93.focusapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.theme.FocusDimens

/** White card with a hairline border, the basic container on light screens. */
@Composable
fun FocusCard(
    modifier: Modifier = Modifier,
    color: Color = FocusColors.Surface,
    borderColor: Color? = FocusColors.Border,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(FocusDimens.CardRadius),
        color = color,
        border = borderColor?.let { BorderStroke(1.dp, it) },
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}
