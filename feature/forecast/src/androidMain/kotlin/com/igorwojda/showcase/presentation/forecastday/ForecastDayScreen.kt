package com.igorwojda.showcase.presentation.forecastday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igorwojda.showcase.domain.model.DailyWeatherModel
import com.igorwojda.showcase.domain.model.HourlyTemperatureModel
import com.igorwojda.showcase.presentation.common.ErrorContent
import com.igorwojda.showcase.presentation.common.format
import com.igorwojda.showcase.presentation.common.fullDateFormatter
import com.igorwojda.showcase.presentation.common.timeFormatter
import com.igorwojda.showcase.presentation.forecast.WeatherCondition
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastDayScreen(
    date: LocalDate,
    onBack: () -> Unit,
    // Scoped to the back stack entry, so each day gets its own ViewModel.
    viewModel: ForecastDayViewModel = koinViewModel { parametersOf(date) },
) {
    val store = viewModel.store
    val state by store.subscribe()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(date.format(fullDateFormatter)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            when (val currentState = state) {
                ForecastDayState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                is ForecastDayState.Content -> ForecastDayContent(
                    day = currentState.day,
                    modifier = Modifier.fillMaxSize(),
                )

                is ForecastDayState.Error -> ErrorContent(
                    message = currentState.message,
                    onRetry = { store.intent(ForecastDayIntent.Retry) },
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun ForecastDayContent(
    day: DailyWeatherModel,
    modifier: Modifier = Modifier,
) {
    val condition = WeatherCondition.fromCode(day.weatherCode)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = condition.symbol, fontSize = 64.sp)
                Text(
                    text = "${day.temperatureMax.roundToInt()}${day.temperatureUnit}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Low ${day.temperatureMin.roundToInt()}${day.temperatureUnit}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = condition.label,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        if (day.hourlyTemperatures.isNotEmpty()) {
            Text(
                text = "Hourly",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            HourlyTemperatureRow(
                hours = day.hourlyTemperatures,
                temperatureUnit = day.temperatureUnit,
            )
        }

        DetailRow(label = "Precipitation", value = "${day.precipitationSum} ${day.precipitationUnit}")
        DetailRow(label = "Chance of precipitation", value = day.precipitationProbabilityMax?.let { "$it%" } ?: "–")
        DetailRow(label = "Max wind", value = "${day.windSpeedMax.roundToInt()} ${day.windSpeedUnit}")
        DetailRow(label = "Sunrise", value = day.sunrise.format(timeFormatter))
        DetailRow(label = "Sunset", value = day.sunset.format(timeFormatter))
    }
}

@Composable
private fun HourlyTemperatureRow(
    hours: List<HourlyTemperatureModel>,
    temperatureUnit: String,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(hours) { hour ->
            Card {
                Column(
                    modifier = Modifier
                        .width(64.dp)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = hour.time.format(timeFormatter),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "${hour.temperature.roundToInt()}$temperatureUnit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForecastDayContentPreview() {
    MaterialTheme {
        ForecastDayContent(
            day = DailyWeatherModel(
                date = LocalDate(2026, 9, 23),
                temperatureMin = 9.5,
                temperatureMax = 17.0,
                temperatureUnit = "°C",
                weatherCode = 61,
                sunrise = LocalDateTime(2026, 9, 23, 6, 32),
                sunset = LocalDateTime(2026, 9, 23, 18, 41),
                precipitationSum = 4.2,
                precipitationUnit = "mm",
                precipitationProbabilityMax = 80,
                windSpeedMax = 22.0,
                windSpeedUnit = "km/h",
                hourlyTemperatures = (0..23).map { hour ->
                    HourlyTemperatureModel(
                        time = LocalDateTime(2026, 9, 23, hour, 0),
                        temperature = 9.5 + hour % 12 * 0.6,
                    )
                },
            ),
        )
    }
}
