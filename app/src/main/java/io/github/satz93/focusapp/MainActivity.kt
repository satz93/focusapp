package io.github.satz93.focusapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import io.github.satz93.focusapp.data.focus.FocusNotifications
import io.github.satz93.focusapp.ui.navigation.FocusNavHost
import io.github.satz93.focusapp.ui.theme.FocusTheme

class MainActivity : ComponentActivity() {

    /** Screen requested by a notification tap (timer or session complete). */
    private val requestedRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handleIntent(intent)
        setContent {
            FocusTheme {
                FocusNavHost(
                    requestedRoute = requestedRoute.value,
                    onRouteHandled = { requestedRoute.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra(FocusNotifications.EXTRA_ROUTE)?.let { requestedRoute.value = it }
        intent?.removeExtra(FocusNotifications.EXTRA_ROUTE)
    }
}
