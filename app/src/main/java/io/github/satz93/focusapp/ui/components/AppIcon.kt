package io.github.satz93.focusapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.FocusApplication
import io.github.satz93.focusapp.ui.theme.FocusColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The app's real icon; falls back to an initial on a muted tile (as in the design). */
@Composable
fun AppIcon(pkg: String, label: String, modifier: Modifier = Modifier, size: Dp = 36.dp) {
    val provider = (LocalContext.current.applicationContext as FocusApplication).container.appInfoProvider
    val icon by produceState<ImageBitmap?>(initialValue = null, pkg) {
        value = withContext(Dispatchers.IO) { provider.icon(pkg) }
    }
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier.size(size).clip(shape).background(FocusColors.MutedFill, shape),
        contentAlignment = Alignment.Center,
    ) {
        val bmp = icon
        if (bmp != null) {
            Image(bitmap = bmp, contentDescription = null, modifier = Modifier.size(size))
        } else {
            Text(
                label.take(1).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = FocusColors.Text,
            )
        }
    }
}
