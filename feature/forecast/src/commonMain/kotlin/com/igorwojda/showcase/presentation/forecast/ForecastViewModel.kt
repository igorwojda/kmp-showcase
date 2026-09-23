package com.igorwojda.showcase.presentation.forecast

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

class ForecastViewModel(
    private val forecastRepository: ForecastRepository,
) : StoreViewModel<ForecastState, ForecastIntent, ForecastAction>() {

    override val store = configuredStore(initial = ForecastState.Loading, name = "Forecast") {
        recover { e ->
            updateState { ForecastState.Error(e.message ?: "Unknown error") }
            null // exception handled – don't rethrow
        }

        init { loadForecast() }

        reduce { intent ->
            when (intent) {
                ForecastIntent.Reload -> {
                    loadForecast(forceRefresh = true)
                    action(ForecastAction.ShowToast("Reloaded"))
                }
            }
        }
    }

    private suspend fun PipelineContext<ForecastState, ForecastIntent, ForecastAction>.loadForecast(
        forceRefresh: Boolean = false,
    ) {
        updateState { ForecastState.Loading }
        val forecast = forecastRepository.getForecast(forceRefresh = forceRefresh)
        updateState { ForecastState.Content(forecast) }
    }
}

sealed interface ForecastState : MVIState {
    data object Loading : ForecastState
    data class Content(val forecast: ForecastModel) : ForecastState
    data class Error(val message: String) : ForecastState
}

sealed interface ForecastIntent : MVIIntent {
    data object Reload : ForecastIntent
}

sealed interface ForecastAction : MVIAction {
    data class ShowToast(val message: String) : ForecastAction
}
