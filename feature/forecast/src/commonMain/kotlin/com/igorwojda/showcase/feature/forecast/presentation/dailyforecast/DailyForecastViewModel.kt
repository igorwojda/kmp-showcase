package com.igorwojda.showcase.feature.forecast.presentation.dailyforecast

import com.igorwojda.showcase.feature.base.presentation.flowmvi.StoreViewModel
import com.igorwojda.showcase.feature.base.presentation.flowmvi.configuredStore
import com.igorwojda.showcase.feature.forecast.domain.model.DailyWeatherModel
import com.igorwojda.showcase.feature.forecast.domain.usecase.GetDailyWeatherUseCase
import kotlinx.datetime.LocalDate
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce

/** Weather for a single [date]; reuses the cached forecast via [GetDailyWeatherUseCase]. */
class DailyForecastViewModel internal constructor(
    private val date: LocalDate,
    private val getDailyWeatherUseCase: GetDailyWeatherUseCase,
) : StoreViewModel<DailyForecastState, DailyForecastIntent, DailyForecastAction>() {
    override val store =
        configuredStore(initial = DailyForecastState.Loading, name = "DailyForecast") {
            recover { e ->
                updateState { DailyForecastState.Error(e.message ?: "Unknown error") }
                null // exception handled – don't rethrow
            }

            init { loadDay() }

            reduce { intent ->
                when (intent) {
                    DailyForecastIntent.Retry -> loadDay()
                }
            }
        }

    private suspend fun PipelineContext<DailyForecastState, DailyForecastIntent, DailyForecastAction>.loadDay() {
        updateState { DailyForecastState.Loading }
        val day = getDailyWeatherUseCase(date)
        updateState {
            if (day == null) DailyForecastState.Error("No forecast for $date") else DailyForecastState.Content(day)
        }
    }
}

sealed interface DailyForecastState : MVIState {
    data object Loading : DailyForecastState

    data class Content(
        val day: DailyWeatherModel,
    ) : DailyForecastState

    data class Error(
        val message: String,
    ) : DailyForecastState
}

sealed interface DailyForecastIntent : MVIIntent {
    data object Retry : DailyForecastIntent
}

/** The screen has no one-off events yet. */
sealed interface DailyForecastAction : MVIAction
