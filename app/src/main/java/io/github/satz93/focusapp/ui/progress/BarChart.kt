package io.github.satz93.focusapp.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.satz93.focusapp.ui.theme.FocusColors

/**
 * Minimal bar chart drawn with Canvas. [values] null = no bar (future day / no data).
 * [reference] draws a dashed line (target or goal). Tapping a bar calls [onBarClick].
 */
@Composable
fun BarChart(
    values: List<Float?>,
    labels: List<String>,
    reference: Float?,
    barColor: (index: Int, value: Float) -> Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 120.dp,
    highlightIndex: Int? = null,
    onBarClick: ((Int) -> Unit)? = null,
) {
    val maxValue = maxOf(values.maxOf { it ?: 0f }, (reference ?: 0f) * 1.15f, 1f)
    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .then(
                    if (onBarClick == null) Modifier
                    else Modifier.pointerInput(values.size) {
                        detectTapGestures { pos ->
                            val i = (pos.x / (size.width / values.size)).toInt().coerceIn(0, values.lastIndex)
                            onBarClick(i)
                        }
                    },
                ),
        ) {
            val slot = size.width / values.size
            val barWidth = slot * 0.62f
            values.forEachIndexed { i, v ->
                val x = i * slot + (slot - barWidth) / 2
                if (v == null) return@forEachIndexed
                val h = if (v <= 0f) 3.dp.toPx() else (v / maxValue) * size.height
                drawRoundRect(
                    color = if (v <= 0f) FocusColors.MutedFill else barColor(i, v),
                    topLeft = Offset(x, size.height - h),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                )
            }
            reference?.let { ref ->
                val y = size.height - (ref / maxValue) * size.height
                drawLine(
                    color = FocusColors.Text,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            labels.forEachIndexed { i, label ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (i == highlightIndex) FocusColors.Text else FocusColors.TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
