package io.github.satz93.focusapp.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import java.util.concurrent.ConcurrentHashMap

/** App labels and icons from PackageManager, cached in memory. */
class AppInfoProvider(context: Context) {
    private val pm: PackageManager = context.applicationContext.packageManager
    private val labels = ConcurrentHashMap<String, String>()
    private val icons = ConcurrentHashMap<String, ImageBitmap>()
    private val missingIcons = ConcurrentHashMap.newKeySet<String>()

    fun label(pkg: String): String = labels.getOrPut(pkg) {
        runCatching { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }
            .getOrElse { pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() } }
    }

    /** Call off the main thread. Returns null for uninstalled apps. */
    fun icon(pkg: String, sizePx: Int = 96): ImageBitmap? {
        icons[pkg]?.let { return it }
        if (pkg in missingIcons) return null
        return runCatching { pm.getApplicationIcon(pkg).toBitmap(sizePx, sizePx).asImageBitmap() }
            .onSuccess { icons[pkg] = it }
            .onFailure { missingIcons += pkg }
            .getOrNull()
    }

    fun launcherPackages(): Set<String> {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return pm.queryIntentActivities(home, 0).map { it.activityInfo.packageName }.toSet()
    }
}
