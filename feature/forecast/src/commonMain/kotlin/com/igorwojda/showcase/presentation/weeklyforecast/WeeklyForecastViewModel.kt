package com.igorwojda.showcase.presentation.weeklyforecast

import com.igorwojda.showcase.data.ForecastRepository
import com.igorwojda.showcase.domain.model.ForecastModel
import com.igorwojda.showcase.feature.base.presentation.flowmvi.StoreViewModel
import com.igorwojda.showcase.feature.base.presentation.flowmvi.configuredStore
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce

class WeeklyForecastViewModel(
    private val forecastRepository: ForecastRepository,
) : StoreViewModel<WeeklyForecastState, WeeklyForecastIntent, WeeklyForecastAction>() {
    override val store =
        configuredStore(initial = WeeklyForecastState.Loading, name = "WeeklyForecast") {
            recover { e ->
                updateState { WeeklyForecastState.Error(e.message ?: "Unknown error") }
                null // exception handled – don't rethrow
            }

            init { loadForecast() }

            reduce { intent ->
                when (intent) {
                    WeeklyForecastIntent.Reload -> {
                        loadForecast(forceRefresh = true)
                        action(WeeklyForecastAction.ShowToast("Reloaded"))
                    }
                }
            }
        }

    private suspend fun PipelineContext<WeeklyForecastState, WeeklyForecastIntent, WeeklyForecastAction>.loadForecast(
        forceRefresh: Boolean = false,
    ) {
        updateState { WeeklyForecastState.Loading }
        val forecast = forecastRepository.getForecast(forceRefresh = forceRefresh)
        updateState { WeeklyForecastState.Content(forecast) }
    }
}

sealed interface WeeklyForecastState : MVIState {
    data object Loading : WeeklyForecastState

    data class Content(
        val forecast: ForecastModel,
    ) : WeeklyForecastState

    data class Error(
        val message: String,
    ) : WeeklyForecastState
}

sealed interface WeeklyForecastIntent : MVIIntent {
    data object Reload : WeeklyForecastIntent
}

sealed interface WeeklyForecastAction : MVIAction {
    data class ShowToast(
        val message: String,
    ) : WeeklyForecastAction
}
