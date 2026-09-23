package com.igorwojda.showcase.presentation.forecast

import com.igorwojda.showcase.data.ForecastRepository
import com.igorwojda.showcase.domain.model.ForecastModel
import com.rickclephas.kmp.observableviewmodel.coroutineScope
import pro.respawn.flowmvi.api.ActionShareBehavior
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

class ForecastViewModel(
    private val forecastRepository: ForecastRepository,
) : StoreViewModel<ForecastState, ForecastIntent, ForecastAction>() {

    override val store = store(
        initial = ForecastState.Loading,
        scope = viewModelScope.coroutineScope,
    ) {
        configure {
            name = "Forecast"
            debuggable = true
            // Actions are delivered to a single subscriber (the screen) – the default behavior.
            actionShareBehavior = ActionShareBehavior.Distribute()
        }

        enableLogging()
        enableRemoteDebugging(host = "127.0.0.1")

        recover { e ->
            updateState { ForecastState.Error(e.message ?: "Unknown error") }
            null // exception handled – don't rethrow
        }

        init { loadForecast() }

        reduce { intent ->
            when (intent) {
                ForecastIntent.Reload -> {
                    loadForecast()
                    action(ForecastAction.ShowToast("Reloaded"))
                }
            }
        }
    }

    private suspend fun PipelineContext<ForecastState, ForecastIntent, ForecastAction>.loadForecast() {
        updateState { ForecastState.Loading }
        val forecast = forecastRepository.getForecast()
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
