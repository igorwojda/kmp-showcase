package com.igorwojda.showcase.feature.base.presentation.flowmvi

import com.rickclephas.kmp.observableviewmodel.coroutineScope
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.debugger.plugin.enableRemoteDebugging
import pro.respawn.flowmvi.dsl.BuildStore
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.enableLogging

/**
 * Builds a store launched in the ViewModel's scope, with the configuration shared by every feature
 * (name, logging, remote debugging). [block] adds the feature's own plugins, e.g. `init`, `reduce`, `recover`.
 */
inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreViewModel<S, I, A>.configuredStore(
    initial: S,
    name: String,
    crossinline block: BuildStore<S, I, A>,
): Store<S, I, A> =
    store(initial, scope = viewModelScope.coroutineScope) {
        configure {
            this.name = name
            debuggable = true
        }

        enableLogging()
        enableRemoteDebugging(host = "127.0.0.1")

        block()
    }
