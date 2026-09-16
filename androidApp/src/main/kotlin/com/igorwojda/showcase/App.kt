package com.igorwojda.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.igorwojda.showcase.flowmvi.GreetingIntent
import com.igorwojda.showcase.flowmvi.GreetingState
import com.igorwojda.showcase.flowmvi.HomeViewModelFlowMvi
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun App() {
    MaterialTheme {
//        HomeScreenStateFlow()
        HomeScreenFlowMvi()
    }
}

@Composable
fun HomeScreenStateFlow(
    homeViewModelStateFlow: HomeViewModelStateFlow = viewModel()
) {
    val greetings by homeViewModelStateFlow.greetingList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(all = 10.dp)
            .safeContentPadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        greetings.forEach { greeting ->
            Text(greeting)
            HorizontalDivider()
        }
    }
}

@Composable
fun HomeScreenFlowMvi(
    viewModel: HomeViewModelFlowMvi = viewModel()
) {
    val store = viewModel.store
    val state by store.subscribe()

    Box(
        modifier = Modifier
            .padding(all = 10.dp)
            .safeContentPadding()
            .fillMaxSize(),
    ) {
        when (val s = state) {
            GreetingState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )

            is GreetingState.Content -> Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                s.greetings.forEach { greeting ->
                    Text(greeting)
                    HorizontalDivider()
                }
            }

            is GreetingState.Error -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { store.intent(GreetingIntent.Reload) }) {
                    Text("Reload")
                }
            }
        }
    }
}
