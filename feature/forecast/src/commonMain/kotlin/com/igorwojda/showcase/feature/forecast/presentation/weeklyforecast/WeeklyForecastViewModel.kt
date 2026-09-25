package com.igorwojda.showcase.feature.forecast.presentation.weeklyforecast

import com.igorwojda.showcase.feature.base.presentation.flowmvi.StoreViewModel
import com.igorwojda.showcase.feature.base.presentation.flowmvi.configuredStore
import com.igorwojda.showcase.feature.forecast.domain.model.ForecastModel
import com.igorwojda.showcase.feature.forecast.domain.usecase.GetForecastUseCase
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce

class WeeklyForecastViewModel internal constructor(
    private val getForecastUseCase: GetForecastUseCase,
) : StoreViewModel<WeeklyForecastState, WeeklyForecastIntent, WeeklyForecastAction>() {
    override val store =
        configuredStore(initial = WeeklyForecastState.Loading, name = "WeeklyForecast") {
            recover { e ->
                val message = e.message ?: "Unknown error"
                withState {
                    if (this is WeeklyForecastState.Content) {
                        // A failed pull-to-refresh keeps the current forecast on screen.
                        updateState<WeeklyForecastState.Content, _> { copy(isRefreshing = false) }
                        action(WeeklyForecastAction.ShowToast("Couldn't refresh: $message"))
                    } else {
                        updateState { WeeklyForecastState.Error(message) }
                    }
                }
                null // exception handled – don't rethrow
            }

            init { loadForecast() }

            reduce { intent ->
                when (intent) {
                    WeeklyForecastIntent.Retry -> loadForecast()
                    WeeklyForecastIntent.Refresh -> refreshForecast()
                }
            }
        }

    private suspend fun PipelineContext<WeeklyForecastState, WeeklyForecastIntent, WeeklyForecastAction>.loadForecast() {
        updateState { WeeklyForecastState.Loading }
        val forecast = getForecastUseCase()
        updateState { WeeklyForecastState.Content(forecast) }
    }

    /** Pull-to-refresh: bypasses the cache and keeps the current forecast on screen while loading. */
    private suspend fun PipelineContext<WeeklyForecastState, WeeklyForecastIntent, WeeklyForecastAction>.refreshForecast() {
        updateState<WeeklyForecastState.Content, _> { copy(isRefreshing = true) }
        val forecast = getForecastUseCase(forceRefresh = true)
        updateState { WeeklyForecastState.Content(forecast) }
    }
}

sealed interface WeeklyForecastState : MVIState {
    data object Loading : WeeklyForecastState

    data class Content(
        val forecast: ForecastModel,
        val isRefreshing: Boolean = false,
    ) : WeeklyForecastState

    data class Error(
        val message: String,
    ) : WeeklyForecastState
}

sealed interface WeeklyForecastIntent : MVIIntent {
    /** Error screen's retry button. */
    data object Retry : WeeklyForecastIntent

    /** Pull-to-refresh on the loaded forecast. */
    data object Refresh : WeeklyForecastIntent
}

sealed interface WeeklyForecastAction : MVIAction {
    data class ShowToast(
        val message: String,
    ) : WeeklyForecastAction
}
