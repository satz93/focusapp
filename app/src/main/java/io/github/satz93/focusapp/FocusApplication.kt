package io.github.satz93.focusapp

import android.app.Application
import io.github.satz93.focusapp.di.AppContainer

class FocusApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
