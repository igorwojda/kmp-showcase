@file:OptIn(InternalFlowMVIAPI::class)

package com.igorwojda.showcase.feature.base.presentation

import com.rickclephas.kmp.observableviewmodel.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

/**
 * Base ViewModel that owns a FlowMVI [pro.respawn.flowmvi.api.Container.store] and adds typed entry points for the iOS consumer.
 *
 * `Store` is an interface (an ObjC protocol), so its generics are erased in Swift. [states], [actions]
 * and [sendIntent] re-expose the store with concrete [S], [I] and [A] types.
 *
 * - **iOS consumer** (Swift): use [states], [actions] and [sendIntent].
 * - **Android consumer** (Kotlin): use [pro.respawn.flowmvi.api.Container.store] directly, e.g. `store.subscribe { … }` and `store.intent(…)`.
 */
abstract class StoreViewModel<S : MVIState, I : MVIIntent, A : MVIAction> :
    ViewModel(),
    Container<S, I, A> {

    /**
     * iOS consumer only: the store's state, typed as [S]. SKIE turns it into a Swift `AsyncSequence`.
     * Android consumer should use [store] directly.
     */
    val states: StateFlow<S> get() = store.states

    /**
     * iOS consumer only: one-off [A] actions as a cold flow. SKIE turns it into a Swift `AsyncSequence`.
     * Android consumer should use [store] directly.
     *
     * Collecting opens a real store subscription, so [pro.respawn.flowmvi.api.ActionShareBehavior.Distribute] still sees the
     * screen arrive and leave. Inside `subscribe`, `actions` is the store's flow, not this property.
     */
    val actions: Flow<A>
        get() = channelFlow {
            with(store) { subscribe { actions.collect { send(it) } } }.join()
        }

    /**
     * iOS consumer only: sends [intent] to the store. Typed as [I], unlike `store.intent`, which accepts
     * any [MVIIntent] in Swift. Android consumer should call `store.intent(…)` directly.
     */
    fun sendIntent(intent: I) = store.intent(intent)
}
