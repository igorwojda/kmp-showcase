package com.igorwojda.showcase.presentation.weeklyforecast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igorwojda.showcase.domain.model.CurrentWeatherModel
import com.igorwojda.showcase.presentation.common.format
import com.igorwojda.showcase.presentation.common.timeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun CurrentWeatherCard(
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
