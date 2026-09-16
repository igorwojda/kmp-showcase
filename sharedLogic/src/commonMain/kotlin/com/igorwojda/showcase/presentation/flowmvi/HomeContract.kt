package com.igorwojda.showcase.presentation.flowmvi

import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

/** LCE (Loading / Content / Error) state exposed by [HomeViewModelFlowMvi]. */
sealed interface HomeState : MVIState {
    data object Loading : HomeState
    data class Content(val launchPhrase: String) : HomeState
    data class Error(val message: String) : HomeState
}

/** User intents handled by [HomeViewModelFlowMvi]. */
sealed interface HomeIntent : MVIIntent {
    data object Reload : HomeIntent
}
