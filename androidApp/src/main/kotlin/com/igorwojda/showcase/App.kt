package com.igorwojda.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.igorwojda.showcase.presentation.forecast.ForecastAction
import com.igorwojda.showcase.presentation.forecast.ForecastIntent
import com.igorwojda.showcase.presentation.forecast.ForecastState
import com.igorwojda.showcase.presentation.forecast.ForecastViewModel
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun App() {
    MaterialTheme {
        ForecastScreenFlowMvi()
    }
}

@Composable
fun ForecastScreenFlowMvi(
    viewModel: ForecastViewModel = viewModel()
) {
    val store = viewModel.store
    val context = LocalContext.current

    // The lambda consumes MVIActions as they arrive; it only runs while the UI is visible.
    val state by store.subscribe { action ->
        when (action) {
            is ForecastAction.ShowToast -> Toast.makeText(context, action.message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .padding(all = 10.dp)
            .safeContentPadding()
            .fillMaxSize(),
    ) {
        when (val s = state) {
            ForecastState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )

            is ForecastState.Content -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = s.launchPhrase)
                Button(onClick = { store.intent(ForecastIntent.Reload) }) {
                    Text("Reload")
                }
            }

            is ForecastState.Error -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { store.intent(ForecastIntent.Reload) }) {
                    Text("Reload")
                }
            }
        }
    }
}
