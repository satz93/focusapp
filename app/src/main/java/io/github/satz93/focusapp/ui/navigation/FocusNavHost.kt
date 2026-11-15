package io.github.satz93.focusapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import io.github.satz93.focusapp.FocusApplication
import io.github.satz93.focusapp.ui.appViewModel
import io.github.satz93.focusapp.ui.focus.FocusTimerScreen
import io.github.satz93.focusapp.ui.focus.SessionCompleteScreen
import io.github.satz93.focusapp.ui.home.HomeScreen
import io.github.satz93.focusapp.ui.home.HomeViewModel
import io.github.satz93.focusapp.ui.onboarding.OnboardingPlaceholderStep
import io.github.satz93.focusapp.ui.onboarding.OnboardingViewModel
import io.github.satz93.focusapp.ui.onboarding.WelcomeScreen
import io.github.satz93.focusapp.ui.progress.ProgressScreen
import io.github.satz93.focusapp.ui.theme.FocusColors
import kotlinx.coroutines.flow.first

@Composable
fun FocusNavHost() {
    val context = LocalContext.current
    val settingsRepo = remember { (context.applicationContext as FocusApplication).container.settingsRepository }

    // Decide the start destination once, from persisted onboarding state.
    val start by produceState<String?>(initialValue = null) {
        value = if (settingsRepo.settings.first().onboardingDone) Routes.HOME else Routes.ONBOARDING
    }
    val startDestination = start
    if (startDestination == null) {
        Box(Modifier.fillMaxSize()) // brief blank frame while DataStore loads
        return
    }

    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        containerColor = FocusColors.Background,
        bottomBar = { if (currentRoute in Routes.bottomBarRoutes) BottomBar(nav, currentRoute) },
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = inner.calculateBottomPadding()),
        ) {
            onboardingGraph(nav)

            composable(Routes.HOME) {
                val vm = appViewModel { HomeViewModel(it.settingsRepository) }
                HomeScreen(vm, onStartSession = { nav.navigate(Routes.FOCUS_TIMER) })
            }
            composable(Routes.PROGRESS) { ProgressScreen() }
            composable(Routes.FOCUS_TIMER) {
                FocusTimerScreen(
                    onClose = { nav.popBackStack() },
                    onComplete = {
                        nav.navigate(Routes.SESSION_COMPLETE) { popUpTo(Routes.HOME) }
                    },
                )
            }
            composable(Routes.SESSION_COMPLETE) {
                SessionCompleteScreen(
                    onStartNext = { nav.navigate(Routes.FOCUS_TIMER) { popUpTo(Routes.HOME) } },
                    onBackHome = { nav.popBackStack(Routes.HOME, inclusive = false) },
                )
            }
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.onboardingGraph(nav: NavHostController) {
    navigation(startDestination = Routes.WELCOME, route = Routes.ONBOARDING) {
        composable(Routes.WELCOME) {
            WelcomeScreen(onGetStarted = { nav.navigate(Routes.USAGE_ACCESS) })
        }
        composable(Routes.USAGE_ACCESS) {
            OnboardingPlaceholderStep(
                step = 0,
                title = "First, let's see your real numbers",
                milestone = 2,
                primaryText = "Allow usage access",
                onBack = { nav.popBackStack() },
                onNext = { nav.navigate(Routes.UNLOCKS) },
            )
        }
        composable(Routes.UNLOCKS) {
            OnboardingPlaceholderStep(
                step = 1, title = "You unlock your phone N times a day", milestone = 2,
                onBack = { nav.popBackStack() }, onNext = { nav.navigate(Routes.TOP_APPS) },
            )
        }
        composable(Routes.TOP_APPS) {
            OnboardingPlaceholderStep(
                step = 2, title = "Where your time goes", milestone = 2,
                onBack = { nav.popBackStack() }, onNext = { nav.navigate(Routes.SCREEN_TIME_GOAL) },
            )
        }
        composable(Routes.SCREEN_TIME_GOAL) {
            OnboardingPlaceholderStep(
                step = 3, title = "Let's win some of that time back", milestone = 3, primaryText = "Continue",
                onBack = { nav.popBackStack() }, onNext = { nav.navigate(Routes.FOCUS_TARGET) },
            )
        }
        composable(Routes.FOCUS_TARGET) {
            OnboardingPlaceholderStep(
                step = 4, title = "Put that time into focused work", milestone = 3, primaryText = "Continue",
                onBack = { nav.popBackStack() }, onNext = { nav.navigate(Routes.DISTRACTING_APPS) },
            )
        }
        composable(Routes.DISTRACTING_APPS) {
            val vm = onboardingViewModel(nav)
            OnboardingPlaceholderStep(
                step = 5, title = "Which apps distract you?", milestone = 3, primaryText = "Finish setup",
                onBack = { nav.popBackStack() },
                onNext = {
                    vm.finish {
                        nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                    }
                },
            )
        }
    }
}

/** One ViewModel shared by every onboarding step, scoped to the onboarding graph. */
@Composable
private fun onboardingViewModel(nav: NavHostController): OnboardingViewModel {
    val parent = remember(nav) { nav.getBackStackEntry(Routes.ONBOARDING) }
    return appViewModel(owner = parent) { OnboardingViewModel(it.settingsRepository) }
}

@Composable
private fun BottomBar(nav: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = FocusColors.Surface, tonalElevation = 0.dp) {
        BottomItem(nav, currentRoute, Routes.HOME, "Home", Icons.Outlined.Home)
        BottomItem(nav, currentRoute, Routes.PROGRESS, "Progress", Icons.Outlined.BarChart)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomItem(
    nav: NavHostController,
    currentRoute: String?,
    route: String,
    label: String,
    icon: ImageVector,
) {
    NavigationBarItem(
        selected = currentRoute == route,
        onClick = {
            if (currentRoute != route) {
                nav.navigate(route) {
                    // Home is always the root of the main section (onboarding is popped when it finishes).
                    popUpTo(Routes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = FocusColors.Accent,
            selectedTextColor = FocusColors.Accent,
            indicatorColor = FocusColors.Surface,
            unselectedIconColor = FocusColors.TextSecondary,
            unselectedTextColor = FocusColors.TextSecondary,
        ),
    )
}
