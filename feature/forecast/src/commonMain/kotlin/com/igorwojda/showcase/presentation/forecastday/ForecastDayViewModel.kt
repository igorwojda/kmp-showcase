package com.igorwojda.showcase.presentation.forecastday

import com.igorwojda.showcase.data.ForecastRepository
import com.igorwojda.showcase.domain.model.DailyWeatherModel
import com.igorwojda.showcase.feature.base.presentation.flowmvi.StoreViewModel
import com.igorwojda.showcase.feature.base.presentation.flowmvi.configuredStore
import kotlinx.datetime.LocalDate
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce

/** Weather for a single [date]; reuses the forecast cached by [ForecastRepository]. */
class ForecastDayViewModel(
    private val date: LocalDate,
    private val forecastRepository: ForecastRepository,
) : StoreViewModel<ForecastDayState, ForecastDayIntent, ForecastDayAction>() {

    override val store = configuredStore(initial = ForecastDayState.Loading, name = "ForecastDay") {
        recover { e ->
            updateState { ForecastDayState.Error(e.message ?: "Unknown error") }
            null // exception handled – don't rethrow
        }

        init { loadDay() }

        reduce { intent ->
            when (intent) {
                ForecastDayIntent.Retry -> loadDay()
            }
        }
    }

    private suspend fun PipelineContext<ForecastDayState, ForecastDayIntent, ForecastDayAction>.loadDay() {
        updateState { ForecastDayState.Loading }
        val day = forecastRepository.getDailyWeather(date)
        updateState { ForecastDayState.Content(day) }
    }
}

sealed interface ForecastDayState : MVIState {
    data object Loading : ForecastDayState
    data class Content(val day: DailyWeatherModel) : ForecastDayState
    data class Error(val message: String) : ForecastDayState
}

sealed interface ForecastDayIntent : MVIIntent {
    data object Retry : ForecastDayIntent
}

/** The screen has no one-off events yet. */
sealed interface ForecastDayAction : MVIAction
