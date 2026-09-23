package com.igorwojda.showcase.presentation.forecast

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igorwojda.showcase.domain.model.CurrentWeatherModel
import com.igorwojda.showcase.domain.model.DailyWeatherModel
import com.igorwojda.showcase.domain.model.ForecastModel
import com.igorwojda.showcase.presentation.common.ErrorContent
import com.igorwojda.showcase.presentation.common.dayOfMonthFormatter
import com.igorwojda.showcase.presentation.common.dayOfWeekFormatter
import com.igorwojda.showcase.presentation.common.format
import com.igorwojda.showcase.presentation.common.timeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.androidx.compose.koinViewModel
import pro.respawn.flowmvi.compose.dsl.subscribe
import java.util.Locale
import kotlin.math.roundToInt

private val temperatureGradient = listOf(Color(0xFF4FC3F7), Color(0xFFFFB74D))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    onDayClick: (LocalDate) -> Unit,
    viewModel: ForecastViewModel = koinViewModel(),
) {
    val store = viewModel.store
    val context = LocalContext.current

    // The lambda consumes MVIActions as they arrive; it only runs while the UI is visible.
    val state by store.subscribe { action ->
        when (action) {
            is ForecastAction.ShowToast -> Toast.makeText(context, action.message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather") },
                actions = {
                    TextButton(onClick = { store.intent(ForecastIntent.Reload) }) {
                        Text("Reload")
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
                ForecastState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                is ForecastState.Content -> ForecastContent(
                    forecast = currentState.forecast,
                    onDayClick = onDayClick,
                    modifier = Modifier.fillMaxSize(),
                )

                is ForecastState.Error -> ErrorContent(
                    message = currentState.message,
                    onRetry = { store.intent(ForecastIntent.Reload) },
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun ForecastContent(
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

@Composable
private fun CurrentWeatherCard(
    current: CurrentWeatherModel,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
) {
    val condition = WeatherCondition.fromCode(current.weatherCode)

    Card(
        modifier = modifier.fillMaxWidth(),
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
                text = "${current.temperature.roundToInt()}${current.temperatureUnit}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = condition.label,
                style = MaterialTheme.typography.titleMedium,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                WeatherDetail(
                    label = "Wind",
                    value = "${current.windSpeed.roundToInt()} ${current.windSpeedUnit}",
                )
                WeatherDetail(
                    label = "Updated",
                    value = current.time.format(timeFormatter),
                )
                WeatherDetail(
                    label = "Location",
                    value = coordinatesLabel(latitude, longitude),
                )
            }
        }
    }
}

@Composable
private fun WeatherDetail(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun DailyForecastRow(
    day: DailyWeatherModel,
    dayLabel: String,
    scaleMin: Double,
    scaleMax: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val condition = WeatherCondition.fromCode(day.weatherCode)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.width(72.dp)) {
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = day.date.format(dayOfMonthFormatter),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Text(text = condition.symbol, fontSize = 24.sp)

            Text(
                text = "${day.temperatureMin.roundToInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.width(36.dp),
            )

            TemperatureRangeBar(
                min = day.temperatureMin,
                max = day.temperatureMax,
                scaleMin = scaleMin,
                scaleMax = scaleMax,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = "${day.temperatureMax.roundToInt()}${day.temperatureUnit}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(52.dp),
            )
        }
    }
}

/**
 * Horizontal bar showing where [min]..[max] sits inside the [scaleMin]..[scaleMax] range
 * shared by every day of the forecast.
 */
@Composable
private fun TemperatureRangeBar(
    min: Double,
    max: Double,
    scaleMin: Double,
    scaleMax: Double,
    modifier: Modifier = Modifier,
) {
    val scaleSpan = (scaleMax - scaleMin).takeIf { it > 0.0 } ?: 1.0
    val startFraction = ((min - scaleMin) / scaleSpan).toFloat().coerceIn(0f, 1f)
    val endFraction = ((max - scaleMin) / scaleSpan).toFloat().coerceIn(startFraction, 1f)
    // A single-degree day would otherwise collapse to an invisible bar.
    val fillFraction = (endFraction - startFraction).coerceAtLeast(0.05f)
    val leadingFraction = startFraction.coerceAtMost(1f - fillFraction)
    val trailingFraction = 1f - leadingFraction - fillFraction

    Row(
        modifier = modifier
            .height(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (leadingFraction > 0f) {
            Spacer(modifier = Modifier.weight(leadingFraction))
        }
        Box(
            modifier = Modifier
                .weight(fillFraction)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(Brush.horizontalGradient(temperatureGradient)),
        )
        if (trailingFraction > 0f) {
            Spacer(modifier = Modifier.weight(trailingFraction))
        }
    }
}

/** "Today" / "Tomorrow" for the first two days of the forecast, a weekday name for the rest. */
private fun dayLabel(date: LocalDate, index: Int): String = when (index) {
    0 -> "Today"
    1 -> "Tomorrow"
    else -> date.format(dayOfWeekFormatter)
}

private fun coordinatesLabel(latitude: Double, longitude: Double): String {
    val latitudeHemisphere = if (latitude >= 0) "N" else "S"
    val longitudeHemisphere = if (longitude >= 0) "E" else "W"
    return String.format(
        Locale.getDefault(),
        "%.2f°%s %.2f°%s",
        kotlin.math.abs(latitude),
        latitudeHemisphere,
        kotlin.math.abs(longitude),
        longitudeHemisphere,
    )
}

@Preview(showBackground = true)
@Composable
private fun ForecastContentPreview() {
    MaterialTheme {
        ForecastContent(forecast = previewForecast, onDayClick = {})
    }
}

private val previewForecast = ForecastModel(
    latitude = 52.23,
    longitude = 21.01,
    current = CurrentWeatherModel(
        time = LocalDateTime(2026, 9, 22, 14, 30),
        temperature = 18.4,
        temperatureUnit = "°C",
        windSpeed = 11.2,
        windSpeedUnit = "km/h",
        weatherCode = 2,
    ),
    daily = listOf(
        previewDay(LocalDate(2026, 9, 22), 11.0, 19.0, 2),
        previewDay(LocalDate(2026, 9, 23), 9.5, 17.0, 61),
        previewDay(LocalDate(2026, 9, 24), 8.0, 15.5, 3),
        previewDay(LocalDate(2026, 9, 25), 10.0, 21.0, 0),
        previewDay(LocalDate(2026, 9, 26), 12.0, 23.5, 1),
    ),
)

private fun previewDay(date: LocalDate, min: Double, max: Double, weatherCode: Int) = DailyWeatherModel(
    date = date,
    temperatureMin = min,
    temperatureMax = max,
    temperatureUnit = "°C",
    weatherCode = weatherCode,
    sunrise = LocalDateTime(date.year, date.month, date.day, 6, 32),
    sunset = LocalDateTime(date.year, date.month, date.day, 18, 41),
    precipitationSum = 0.0,
    precipitationUnit = "mm",
    precipitationProbabilityMax = 10,
    windSpeedMax = 14.0,
    windSpeedUnit = "km/h",
    hourlyTemperatures = emptyList(),
)
