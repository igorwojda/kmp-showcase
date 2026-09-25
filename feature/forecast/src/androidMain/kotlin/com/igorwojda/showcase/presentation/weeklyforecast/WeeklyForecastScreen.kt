package com.igorwojda.showcase.presentation.weeklyforecast

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.domain.model.CurrentWeatherModel
import com.igorwojda.showcase.domain.model.DailyWeatherModel
import com.igorwojda.showcase.domain.model.ForecastModel
import com.igorwojda.showcase.presentation.common.ErrorContent
import com.igorwojda.showcase.presentation.common.dayOfWeekFormatter
import com.igorwojda.showcase.presentation.common.format
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.androidx.compose.koinViewModel
import pro.respawn.flowmvi.compose.dsl.subscribe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyForecastScreen(
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeeklyForecastViewModel = koinViewModel(),
) {
    val store = viewModel.store
    val context = LocalContext.current

    // The lambda consumes MVIActions as they arrive; it only runs while the UI is visible.
    val state by store.subscribe { action ->
        when (action) {
            is WeeklyForecastAction.ShowToast -> Toast.makeText(context, action.message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Weather") },
            )
        },
    ) { contentPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
        ) {
            when (val currentState = state) {
                WeeklyForecastState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is WeeklyForecastState.Content -> {
                    PullToRefreshBox(
                        isRefreshing = currentState.isRefreshing,
                        onRefresh = { store.intent(WeeklyForecastIntent.Refresh) },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        WeeklyForecastContent(
                            forecast = currentState.forecast,
                            onDayClick = onDayClick,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                is WeeklyForecastState.Error -> {
                    ErrorContent(
                        message = currentState.message,
                        onRetry = { store.intent(WeeklyForecastIntent.Retry) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyForecastContent(
    forecast: ForecastModel,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    // A shared scale keeps the range bars of all days comparable.
    val scaleMin = forecast.daily.minOfOrNull { it.temperatureMin } ?: 0.0
    val scaleMax = forecast.daily.maxOfOrNull { it.temperatureMax } ?: 0.0

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CurrentWeatherCard(
                current = forecast.current,
                latitude = forecast.latitude,
                longitude = forecast.longitude,
            )
        }

        item {
            Text(
                text = "${forecast.daily.size}-day forecast",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        itemsIndexed(forecast.daily) { index, day ->
            DailyForecastRow(
                day = day,
                dayLabel = dayLabel(day.date, index),
                scaleMin = scaleMin,
                scaleMax = scaleMax,
                onClick = { onDayClick(day.date) },
            )
        }
    }
}

/** "Today" / "Tomorrow" for the first two days of the forecast, a weekday name for the rest. */
private fun dayLabel(
    date: LocalDate,
    index: Int,
): String =
    when (index) {
        0 -> "Today"
        1 -> "Tomorrow"
        else -> date.format(dayOfWeekFormatter)
    }

@Preview(showBackground = true)
@Composable
private fun WeeklyForecastContentPreview() {
    MaterialTheme {
        WeeklyForecastContent(forecast = previewForecast, onDayClick = {})
    }
}

private val previewForecast =
    ForecastModel(
        latitude = 52.23,
        longitude = 21.01,
        current =
            CurrentWeatherModel(
                time = LocalDateTime(2026, 9, 22, 14, 30),
                temperature = 18.4,
                temperatureUnit = "°C",
                windSpeed = 11.2,
                windSpeedUnit = "km/h",
                weatherCode = 2,
            ),
        daily =
            listOf(
                previewDay(LocalDate(2026, 9, 22), 11.0, 19.0, 2),
                previewDay(LocalDate(2026, 9, 23), 9.5, 17.0, 61),
                previewDay(LocalDate(2026, 9, 24), 8.0, 15.5, 3),
                previewDay(LocalDate(2026, 9, 25), 10.0, 21.0, 0),
                previewDay(LocalDate(2026, 9, 26), 12.0, 23.5, 1),
            ),
    )

private fun previewDay(
    date: LocalDate,
    min: Double,
    max: Double,
    weatherCode: Int,
) = DailyWeatherModel(
    date = date,
    temperatureMin = min,
    temperatureMax = max,
    temperatureUnit = "°C",
    weatherCode = weatherCode,
    sunrise = LocalDateTime(date.year, date.month, date.day, hour = 6, minute = 32),
    sunset = LocalDateTime(date.year, date.month, date.day, hour = 18, minute = 41),
    precipitationSum = 0.0,
    precipitationUnit = "mm",
    precipitationProbabilityMax = 10,
    windSpeedMax = 14.0,
    windSpeedUnit = "km/h",
    hourlyTemperatures = emptyList(),
)
