package io.github.satz93.focusapp.data.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.satz93.focusapp.FocusApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Fires when a session's time is up, even if the phone is asleep. */
class FocusAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val manager = (context.applicationContext as FocusApplication).container.focusSessionManager
        CoroutineScope(Dispatchers.Default).launch {
            try {
                manager.completeIfDue()
            } finally {
                pending.finish()
            }
        }
    }
}
