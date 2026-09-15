package com.igorwojda.showcase

/** Swift can't read `StateFlow.value` generically; expose it as a plain property. */
val MainViewModel.greetings: List<String>
    get() = greetingList.value
