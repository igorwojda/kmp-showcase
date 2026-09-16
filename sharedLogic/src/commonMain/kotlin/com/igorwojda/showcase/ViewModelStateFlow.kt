package com.igorwojda.showcase

import com.igorwojda.showcase.data.GreetingRepository
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ViewModelStateFlow : ViewModel() {
    val greetingList: StateFlow<List<String>>
        field = MutableStateFlow<List<String>>(viewModelScope, listOf())

    init {
        viewModelScope.launch {
            GreetingRepository().greetFlow().collect { phrase ->
                greetingList.update { list -> list + phrase }
            }
        }
    }
}
