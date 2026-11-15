package io.github.satz93.focusapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.satz93.focusapp.FocusApplication
import io.github.satz93.focusapp.di.AppContainer

/** Creates a ViewModel with access to the [AppContainer] (manual DI). */
@Composable
inline fun <reified VM : ViewModel> appViewModel(
    owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
    crossinline create: (AppContainer) -> VM,
): VM {
    val container = (LocalContext.current.applicationContext as FocusApplication).container
    return viewModel(
        viewModelStoreOwner = owner,
        factory = viewModelFactory { initializer { create(container) } },
    )
}
