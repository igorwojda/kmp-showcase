package com.igorwojda.showcase.feature.base.presentation.flowmvi

import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.stateIn
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.dsl.state

/**
 * Base ViewModel that owns a FlowMVI [Container.store] and adds typed entry points for the iOS consumer.
 *
 * `Store` is an interface (an ObjC protocol), so its generics are erased in Swift. [states], [actions]
 * and [onIntent] re-expose the store with concrete [S], [I] and [A] types.
 *
 * - **iOS consumer** (Swift): use [states], [actions] and [onIntent].
 * - **Android consumer** (Kotlin): use [Container.store] directly, e.g. `store.subscribe { … }` and `store.intent(…)`.
 *
 * [states] and [actions] read the store only through `subscribe`, like the Compose `subscribe` does, so the store
 * counts the iOS screen as a subscriber while it collects them (e.g. for `whileSubscribed`).
 */
abstract class StoreViewModel<S : MVIState, I : MVIIntent, A : MVIAction> :
    ViewModel(),
    Container<S, I, A> {

    /**
     * iOS consumer only: the store's state, typed as [S]. SKIE turns it into a Swift `AsyncSequence`.
     * Android consumer should use [store] directly.
     *
     * Collecting opens a store subscription, closed when the last collector leaves. Lazy, because SKIE's `Observing`
     * keys the view on the flow's identity (a new flow per access would reset it) and [store] isn't set yet while
     * this base class is constructed.
     */
    @OptIn(DelicateStoreApi::class)
    val states: StateFlow<S> by lazy {
        subscription { states }.stateIn(viewModelScope.coroutineScope, SharingStarted.WhileSubscribed(), store.state)
    }

    /**
     * iOS consumer only: one-off [A] actions as a cold flow. SKIE turns it into a Swift `AsyncSequence`.
     * Android consumer should use [store] directly.
     *
     * Collecting opens a store subscription, so [pro.respawn.flowmvi.api.ActionShareBehavior.Distribute] still sees the
     * screen arrive and leave.
     */
    val actions: Flow<A> get() = subscription { actions }

    /**
     * iOS consumer only: sends [intent] to the store. Typed as [I], unlike `store.intent`, which accepts
     * any [MVIIntent] in Swift. Android consumer should call `store.intent(…)` directly.
     */
    fun onIntent(intent: I) = store.intent(intent)

    /**
     * A cold flow backed by its own store subscription, open while the flow is collected.
     * Inside [select], `states` and `actions` are the subscription's flows, not this class's properties.
     */
    private fun <T> subscription(select: Provider<S, I, A>.() -> Flow<T>): Flow<T> = channelFlow {
        with(store) { subscribe { select().collect { send(it) } } }.join()
    }
}
