package com.igorwojda.showcase.flowmvi

import com.igorwojda.showcase.data.GreetingRepository
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.coroutineScope
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce

/**
 * Same data source ([GreetingRepository]), but the state is a single LCE sealed type driven by a
 * FlowMvi [pro.respawn.flowmvi.api.Store]:
 * - `init`    – kicks off loading when the store starts
 * - `reduce`  – handles [GreetingIntent] sent by the UI (`store.intent(Reload)`)
 * - `recover` – maps any exception thrown inside the pipeline to [GreetingState.Error]
 *
 * UI subscribes via `store.states` (or `store.subscribe { … }` from `flowmvi-compose`).
 * No actions (one-off events) are used, hence `Nothing` as the action type.
 */
class HomeViewModelFlowMvi(
    private val repository: GreetingRepository = GreetingRepository(),
) : ViewModel(), Container<GreetingState, GreetingIntent, Nothing> {

    // Store is bound to the ViewModel's scope, so it starts here and stops on onCleared().
    override val store = store(
        initial = GreetingState.Loading,
        scope = viewModelScope.coroutineScope,
    ) {
        configure {
            name = "Greeting"
            debuggable = true
            actionShareBehavior = ActionShareBehavior.Disabled
        }

        recover { e ->
            updateState { GreetingState.Error(e.message ?: "Unknown error") }
            null // exception handled – don't rethrow
        }

        init { loadGreetings() }

        reduce { intent ->
            when (intent) {
                GreetingIntent.Reload -> loadGreetings()
            }
        }
    }

    /** Loading → Content(accumulated); any exception is routed to `recover` above. */
    private suspend fun PipelineContext<GreetingState, GreetingIntent, Nothing>.loadGreetings() {
        updateState { GreetingState.Loading }
        repository.greetFlow().collect { phrase ->
            updateState {
                val current = (this as? GreetingState.Content)?.greetings.orEmpty()
                GreetingState.Content(current + phrase)
            }
        }
    }
}
