package com.igorwojda.showcase.presentation.forecast

import com.rickclephas.kmp.observableviewmodel.coroutineScope
import kotlinx.coroutines.Job

/**
 * Delivers one-off [ForecastAction]s to Swift, which cannot collect Kotlin flows directly.
 *
 * State is observed through [ForecastViewModel.uiState]; this only covers side effects such as toasts.
 * Cancel the returned [Job] when the view disappears so the store sees the subscriber leave.
 */
fun ForecastViewModel.subscribeActions(onAction: (ForecastAction) -> Unit): Job = with(store) {
    viewModelScope.coroutineScope.subscribe {
        actions.collect { onAction(it) }
    }
}
