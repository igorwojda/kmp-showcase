package com.igorwojda.showcase

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.igorwojda.showcase.ui.forecast.ForecastScreen

@Composable
fun App() {
    MaterialTheme {
        ForecastScreen()
    }
}
