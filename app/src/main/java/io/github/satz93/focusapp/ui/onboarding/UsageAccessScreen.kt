package io.github.satz93.focusapp.ui.onboarding

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.satz93.focusapp.ui.components.FocusCard
import io.github.satz93.focusapp.ui.components.OnboardingScaffold
import io.github.satz93.focusapp.ui.theme.FocusColors

@Composable
fun UsageAccessScreen(
    vm: OnboardingViewModel,
    onBack: () -> Unit,
    onGranted: () -> Unit,
) {
    val context = LocalContext.current
    var granted by rememberSaveable { mutableStateOf(vm.hasUsageAccess()) }
    var sentToSettings by rememberSaveable { mutableStateOf(false) }

    // Coming back from the system settings screen: re-check and move on if allowed.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        granted = vm.hasUsageAccess()
        if (granted && sentToSettings) {
            sentToSettings = false
            vm.loadUsageIfNeeded()
            onGranted()
        }
    }

    OnboardingScaffold(
        step = 0,
        onBack = onBack,
        primaryText = if (granted) "Continue" else "Allow usage access",
        onPrimary = {
            if (granted) {
                vm.loadUsageIfNeeded()
                onGranted()
            } else {
                sentToSettings = true
                openUsageAccessSettings(context)
            }
        },
    ) {
        Box(
            Modifier.size(48.dp).background(FocusColors.AccentTint, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Outlined.ShowChart, contentDescription = null, tint = FocusColors.Accent)
        }
        Spacer(Modifier.height(20.dp))
        Text("First, let’s see your real numbers", style = MaterialTheme.typography.headlineMedium, color = FocusColors.Text)
        Spacer(Modifier.height(10.dp))
        Text(
            "To show how you use your phone, the app needs Usage access. It reads:",
            style = MaterialTheme.typography.bodyMedium,
            color = FocusColors.TextSecondary,
        )
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ReadsRow("Time spent in each app")
            ReadsRow("How many times you unlock your phone")
            ReadsRow("When during the day you pick it up")
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = FocusColors.TextSecondary,
                modifier = Modifier.size(16.dp).padding(top = 2.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Your data stays on your phone. We never see your messages, photos or what you watch.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusColors.TextSecondary,
            )
        }
        if (!granted && sentToSettings) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Usage access is still off. Find this app in the list and turn on “Permit usage access”.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusColors.OverLimit,
            )
        }
    }
}

@Composable
private fun ReadsRow(text: String) {
    FocusCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Check, contentDescription = null, tint = FocusColors.Accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = FocusColors.Text)
        }
    }
}

/** Opens this app's page in Usage access settings where supported, else the full list. */
private fun openUsageAccessSettings(context: Context) {
    val direct = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:${context.packageName}"))
    val list = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    try {
        context.startActivity(direct)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(list)
    } catch (_: SecurityException) {
        context.startActivity(list)
    }
}
