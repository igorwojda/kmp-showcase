package com.igorwojda.showcase

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.igorwojda.showcase.presentation.forecastday.ForecastDayScreen
import com.igorwojda.showcase.presentation.forecast.ForecastScreen
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
private data object ForecastRoute : NavKey

@Serializable
private data class ForecastDayRoute(val date: LocalDate) : NavKey

/**
 * Navigation 3 host. The back stack is saved across configuration changes and process death, and
 * `rememberViewModelStoreNavEntryDecorator` scopes each screen's ViewModel to its back stack entry.
 */
@Composable
fun App() {
    val backStack = rememberNavBackStack(ForecastRoute)

    MaterialTheme {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<ForecastRoute> {
                    ForecastScreen(onDayClick = { date -> backStack.add(ForecastDayRoute(date)) })
                }
                entry<ForecastDayRoute> { route ->
                    ForecastDayScreen(
                        date = route.date,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
        )
    }
}
