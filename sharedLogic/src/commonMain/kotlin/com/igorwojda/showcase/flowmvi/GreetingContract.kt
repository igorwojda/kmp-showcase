package com.igorwojda.showcase.flowmvi

import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

/** LCE (Loading / Content / Error) state exposed by [HomeViewModelFlowMvi]. */
sealed interface GreetingState : MVIState {
    data object Loading : GreetingState
    data class Content(val greetings: List<String>) : GreetingState
    data class Error(val message: String) : GreetingState
}

/** User intents handled by [HomeViewModelFlowMvi]. */
sealed interface GreetingIntent : MVIIntent {
    data object Reload : GreetingIntent
}
