package com.igorwojda.showcase.presentation.flowmvi

import com.igorwojda.showcase.data.RocketRepository
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.coroutineScope
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

/**
 * Same data source ([RocketRepository]), but the state is a single LCE sealed type driven by a
 * FlowMvi [pro.respawn.flowmvi.api.Store]:
 * - `init`    – kicks off loading when the store starts
 * - `reduce`  – handles [HomeIntent] sent by the UI (`store.intent(Reload)`)
 * - `recover` – maps any exception thrown inside the pipeline to [HomeState.Error]
 *
 * UI subscribes via `store.states` (or `store.subscribe { … }` from `flowmvi-compose`).
 * [HomeAction] carries one-off events that must not be replayed on recomposition – here a toast
 * confirming a manual reload.
 */
class HomeViewModelFlowMvi(
    private val repository: RocketRepository = RocketRepository(),
) : ViewModel(), Container<HomeState, HomeIntent, HomeAction> {

    // Store is bound to the ViewModel's scope, so it starts here and stops on onCleared().
    override val store = store(
        initial = HomeState.Loading,
        scope = viewModelScope.coroutineScope,
    ) {
        configure {
            name = "Home"
            debuggable = true
            // Actions are delivered to a single subscriber (the screen) – the default behavior.
            actionShareBehavior = ActionShareBehavior.Distribute()
        }

        enableLogging()
        // Streams state/intent/action history to the FlowMVI debugger (Android Studio plugin or desktop app).
        // Throws if `debuggable` is false, so keep the flag above in sync.
        // Host is loopback on every platform: iOS simulator reaches the Mac directly; Android (emulator or
        // USB device) needs `adb reverse tcp:9684 tcp:9684` first – the default `10.0.2.2` is emulator-only
        // and the IDE server listens on 127.0.0.1 only, so a LAN IP is refused.
        enableRemoteDebugging(host = "127.0.0.1")

        recover { e ->
            updateState { HomeState.Error(e.message ?: "Unknown error") }
            null // exception handled – don't rethrow
        }

        init { loadLaunchPhrase() }

        reduce { intent ->
            when (intent) {
                HomeIntent.Reload -> {
                    loadLaunchPhrase()
                    // One-off event: shown once, never replayed when the UI recomposes.
                    action(HomeAction.ShowToast("Reloaded"))
                }
            }
        }
    }

    /** Loading → Content; any exception is routed to `recover` above. */
    private suspend fun PipelineContext<HomeState, HomeIntent, HomeAction>.loadLaunchPhrase() {
        updateState { HomeState.Loading }
        val phrase = repository.launchPhrase()
        updateState { HomeState.Content(phrase) }
    }
}

sealed interface HomeState : MVIState {
    data object Loading : HomeState
    data class Content(val launchPhrase: String) : HomeState
    data class Error(val message: String) : HomeState
}

sealed interface HomeIntent : MVIIntent {
    data object Reload : HomeIntent
}

sealed interface HomeAction : MVIAction {
    data class ShowToast(val message: String) : HomeAction
}
