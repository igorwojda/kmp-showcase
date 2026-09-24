package com.igorwojda.showcase

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.igorwojda.showcase.feature.permission.presentation.location.LocationPermissionScreen
import com.igorwojda.showcase.feature.permission.presentation.location.isLocationPermissionGranted
import com.igorwojda.showcase.presentation.dailyforecast.DailyForecastScreen
import com.igorwojda.showcase.presentation.weeklyforecast.WeeklyForecastScreen
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
private data object LocationPermissionRoute : NavKey

@Serializable
private data object WeeklyForecastRoute : NavKey

@Serializable
private data class DailyForecastRoute(
    val date: LocalDate,
) : NavKey

/**
 * Navigation 3 host. The back stack is saved across configuration changes and process death, and
 * `rememberViewModelStoreNavEntryDecorator` scopes each screen's ViewModel to its back stack entry.
 *
 * The forecast needs the location permission, so the app starts on [LocationPermissionRoute] without it.
 */
@Composable
fun App() {
    val context = LocalContext.current
    val backStack =
        rememberNavBackStack(
            if (context.isLocationPermissionGranted()) WeeklyForecastRoute else LocationPermissionRoute,
        )

    // The permission can be lost while the app is away (revoked in Settings, one-time grant expired, auto-reset
    // of unused apps), and a back stack restored after process death still starts on the forecast.
    LifecycleResumeEffect(backStack) {
        if (!context.isLocationPermissionGranted() && backStack.lastOrNull() != LocationPermissionRoute) {
            backStack.resetTo(LocationPermissionRoute)
        }
        onPauseOrDispose {}
    }

    MaterialTheme {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            entryProvider =
                entryProvider {
                    entry<LocationPermissionRoute> {
                        // Replaced, so Back from the forecast doesn't return to the permission screen.
                        LocationPermissionScreen(onPermissionGrant = { backStack.resetTo(WeeklyForecastRoute) })
                    }
                    entry<WeeklyForecastRoute> {
                        WeeklyForecastScreen(onDayClick = { date -> backStack.add(DailyForecastRoute(date)) })
                    }
                    entry<DailyForecastRoute> { route ->
                        DailyForecastScreen(
                            date = route.date,
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }
                },
        )
    }
}

/** Makes [route] the only entry of the back stack. */
private fun NavBackStack<NavKey>.resetTo(route: NavKey) {
    clear()
    add(route)
}
