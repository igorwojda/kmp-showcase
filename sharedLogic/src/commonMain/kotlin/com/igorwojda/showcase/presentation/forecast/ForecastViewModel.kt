@file:OptIn(InternalFlowMVIAPI::class)

package com.igorwojda.showcase.presentation.forecast

import com.igorwojda.showcase.data.ForecastRepository
import com.igorwojda.showcase.data.model.ForecastModel
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.Container
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
) : ViewModel(), Container<ForecastState, ForecastIntent, ForecastAction> {

    //TODO: // Kotlin/Native doesn't export default arguments to Swift, so expose an explicit no-arg init.
    constructor() : this(ForecastRepository())

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

    //TODO: Needed?
    /** Mirrors `store.states` so SwiftUI (`@StateViewModel`) can observe it without a FlowMVI/Compose bridge. */
    val uiState: StateFlow<ForecastState>
        field = MutableStateFlow(viewModelScope, store.states.value)

    init {
        //TODO: Is there a better way?
        viewModelScope.coroutineScope.launch {
            store.states.collect { uiState.value = it }
        }
    }

    /** Loading → Content; any exception is routed to `recover` above. */
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
