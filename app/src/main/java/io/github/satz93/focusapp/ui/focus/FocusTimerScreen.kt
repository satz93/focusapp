package io.github.satz93.focusapp.ui.focus

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.satz93.focusapp.ui.components.LinkButton
import io.github.satz93.focusapp.ui.components.PrimaryButton
import io.github.satz93.focusapp.ui.theme.FocusColors
import io.github.satz93.focusapp.ui.theme.FocusDimens
import io.github.satz93.focusapp.ui.util.formatClock
import kotlinx.coroutines.delay

private val LengthOptions = listOf(25, 45, 55)

@Composable
fun FocusTimerScreen(
    vm: FocusTimerViewModel,
    onClose: () -> Unit,
    onComplete: () -> Unit,
    onEndedEarly: () -> Unit,
) {
    val active by vm.active.collectAsStateWithLifecycle()
    val pending by vm.pendingComplete.collectAsStateWithLifecycle()
    val ready by vm.ready.collectAsStateWithLifecycle()
    var confirmEnd by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(pending) { if (pending != null) onComplete() }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(active) {
        val s = active ?: return@LaunchedEffect
        while (true) {
            now = System.currentTimeMillis()
            if (!s.isPaused && s.remainingMs(now) <= 0L) vm.tick()
            delay(200)
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        vm.start() // start either way; notifications are optional
    }
    val startSession = {
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        else vm.start()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(FocusColors.DarkBackground)
            .safeDrawingPadding()
            .padding(horizontal = FocusDimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val session = active
        val number = session?.sessionNumber ?: ready.nextNumber
        val length = session?.plannedMin ?: ready.lengthMin
        Box(Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = FocusColors.OnDark)
            }
            Text(
                sessionTitle(number, ready.target, length),
                style = MaterialTheme.typography.titleSmall,
                color = FocusColors.OnDark,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Spacer(Modifier.height(40.dp))

        val progressLeft = session?.let { 1f - it.progress(now) } ?: 1f
        val clock = session?.let { formatClock((it.remainingMs(now) + 999) / 1000) } ?: formatClock(length * 60L)
        val caption = when {
            session == null -> "Ready when you are"
            session.isPaused -> "Paused"
            else -> "Stay with it"
        }
        TimerRing(progressLeft = progressLeft, clock = clock, caption = caption)

        if (session == null) {
            Spacer(Modifier.height(28.dp))
            LengthPicker(selected = ready.lengthMin, onSelect = vm::chooseLength)
        }

        Spacer(Modifier.weight(1f))
        Text(
            if (session == null) "Put your phone face down. The timer keeps running if you leave the app."
            else "You can leave this screen. The timer keeps running and lives in your notifications.",
            style = MaterialTheme.typography.bodyMedium,
            color = FocusColors.OnDarkSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        when {
            session == null -> PrimaryButton(text = "Start focus", onClick = startSession, onDark = true)
            session.isPaused -> PrimaryButton(text = "Resume", onClick = vm::resume, onDark = true)
            else -> PrimaryButton(text = "Pause", onClick = vm::pause, onDark = true)
        }
        Box(Modifier.height(64.dp), contentAlignment = Alignment.Center) {
            if (session != null) {
                LinkButton(text = "End session early", onClick = { confirmEnd = true }, color = FocusColors.AccentOnDark)
            }
        }
    }

    if (confirmEnd && active != null) {
        val focusedMin = (active!!.elapsedMs(now) / 60_000L).toInt()
        AlertDialog(
            onDismissRequest = { confirmEnd = false },
            containerColor = FocusColors.DarkCard,
            titleContentColor = FocusColors.OnDark,
            textContentColor = FocusColors.OnDarkSecondary,
            title = { Text("End this session?") },
            text = {
                Text(
                    "You’ve focused for $focusedMin min. Ending now won’t count toward today’s target.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmEnd = false
                    vm.endEarly(onEndedEarly)
                }) { Text("End session", color = FocusColors.AccentOnDark) }
            },
            dismissButton = {
                TextButton(onClick = { confirmEnd = false }) { Text("Keep going", color = FocusColors.OnDark) }
            },
        )
    }
}

fun sessionTitle(number: Int, target: Int, lengthMin: Int): String =
    if (number <= target) "Session $number of $target · $lengthMin min"
    else "Session $number · $lengthMin min"

@Composable
private fun TimerRing(progressLeft: Float, clock: String, caption: String) {
    Box(Modifier.size(264.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(
                color = FocusColors.DarkLine, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(stroke),
            )
            drawArc(
                color = FocusColors.AccentOnDark, startAngle = -90f, sweepAngle = 360f * progressLeft.coerceIn(0f, 1f),
                useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(clock, style = MaterialTheme.typography.displayMedium, color = FocusColors.OnDark)
            Spacer(Modifier.height(4.dp))
            Text(caption, style = MaterialTheme.typography.bodySmall, color = FocusColors.OnDarkSecondary)
        }
    }
}

@Composable
private fun LengthPicker(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LengthOptions.forEach { min ->
            val isSelected = min == selected
            Surface(
                onClick = { onSelect(min) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) FocusColors.DarkCard else FocusColors.DarkBackground,
                border = BorderStroke(1.dp, if (isSelected) FocusColors.AccentOnDark else FocusColors.DarkLine),
                modifier = Modifier.semantics {
                    role = Role.RadioButton
                    this.selected = isSelected
                },
            ) {
                Text(
                    "$min min",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) FocusColors.OnDark else FocusColors.OnDarkSecondary,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                )
            }
        }
    }
}

