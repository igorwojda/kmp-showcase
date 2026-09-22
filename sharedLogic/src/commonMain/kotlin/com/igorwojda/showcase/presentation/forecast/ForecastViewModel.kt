@file:OptIn(InternalFlowMVIAPI::class)

package com.igorwojda.showcase.presentation.forecast

import com.igorwojda.showcase.data.RocketRepository
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
    private val repository: RocketRepository,
) : ViewModel(), Container<ForecastState, ForecastIntent, ForecastAction> {
    // Kotlin/Native doesn't export default arguments to Swift, so expose an explicit no-arg init.
    constructor() : this(RocketRepository())

    // Store is bound to the ViewModel's scope, so it starts here and stops on onCleared().
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

        init { loadLaunchPhrase() }

        reduce { intent ->
            when (intent) {
                ForecastIntent.Reload -> {
                    loadLaunchPhrase()
                    // One-off event: shown once, never replayed when the UI recomposes.
                    action(ForecastAction.ShowToast("Reloaded"))
                }
            }
        }
    }

    /** Mirrors `store.states` so SwiftUI (`@StateViewModel`) can observe it without a FlowMVI/Compose bridge. */
    val uiState: StateFlow<ForecastState>
        field = MutableStateFlow(viewModelScope, store.states.value)

    init {
        // `scope` was passed to the `store(...)` builder above, so the pipeline already started;
        // just forward each emitted state into `uiState`.
        viewModelScope.coroutineScope.launch {
            store.states.collect { uiState.value = it }
        }
    }

    /** Loading → Content; any exception is routed to `recover` above. */
    private suspend fun PipelineContext<ForecastState, ForecastIntent, ForecastAction>.loadLaunchPhrase() {
        updateState { ForecastState.Loading }
        val phrase = repository.launchPhrase()
        updateState { ForecastState.Content(phrase) }
    }
}

sealed interface ForecastState : MVIState {
    data object Loading : ForecastState
    data class Content(val launchPhrase: String) : ForecastState
    data class Error(val message: String) : ForecastState
}

sealed interface ForecastIntent : MVIIntent {
    data object Reload : ForecastIntent
}

sealed interface ForecastAction : MVIAction {
    data class ShowToast(val message: String) : ForecastAction
}
