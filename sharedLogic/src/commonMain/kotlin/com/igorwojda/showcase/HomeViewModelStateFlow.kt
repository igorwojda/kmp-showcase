package com.igorwojda.showcase

import com.igorwojda.showcase.data.RocketRepository
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.StateFlow

class HomeViewModelStateFlow(
    private val rocketRepository: RocketRepository = RocketRepository(),
) : ViewModel() {
    /** `null` while loading. */
    val launchPhrase: StateFlow<String?>
        field = MutableStateFlow<String?>(viewModelScope, null)

    init {
        viewModelScope.launch {
            launchPhrase.value = runCatching { rocketRepository.launchPhrase() }
                .getOrElse { "Error occurred" }
        }
    }
}
