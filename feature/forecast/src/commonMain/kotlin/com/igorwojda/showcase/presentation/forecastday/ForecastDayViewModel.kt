package com.igorwojda.showcase.presentation.forecastday

import com.igorwojda.showcase.data.ForecastRepository
import com.igorwojda.showcase.domain.model.DailyWeatherModel
import com.igorwojda.showcase.feature.base.presentation.StoreViewModel
import com.rickclephas.kmp.observableviewmodel.coroutineScope
import kotlinx.datetime.LocalDate
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.plugin.enableRemoteDebugging
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce

/** Weather for a single [date]; reuses the forecast cached by [ForecastRepository]. */
class ForecastDayViewModel(
    private val date: LocalDate,
    private val forecastRepository: ForecastRepository,
) : StoreViewModel<ForecastDayState, ForecastDayIntent, ForecastDayAction>() {

    override val store = store(
        initial = ForecastDayState.Loading,
        scope = viewModelScope.coroutineScope,
    ) {
        configure {
            name = "ForecastDay"
            debuggable = true
        }

        enableLogging()
        enableRemoteDebugging(host = "127.0.0.1")

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
