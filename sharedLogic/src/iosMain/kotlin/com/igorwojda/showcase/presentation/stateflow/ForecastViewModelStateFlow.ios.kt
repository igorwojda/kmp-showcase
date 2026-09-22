package com.igorwojda.showcase.presentation.stateflow

/** Swift can't read `StateFlow.value` generically; expose it as a plain property. */
val ForecastViewModelStateFlow.launchPhraseValue: String?
    get() = launchPhrase.value
