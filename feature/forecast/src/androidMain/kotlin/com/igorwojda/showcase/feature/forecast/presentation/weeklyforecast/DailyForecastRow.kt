package com.igorwojda.showcase.feature.forecast.presentation.weeklyforecast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igorwojda.showcase.feature.forecast.domain.model.DailyWeatherModel
import com.igorwojda.showcase.feature.forecast.presentation.common.dayOfMonthFormatter
import com.igorwojda.showcase.feature.forecast.presentation.common.format
import kotlin.math.roundToInt

@Composable
internal fun DailyForecastRow(
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
            modifier =
                Modifier
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
                low = day.temperatureMin,
                high = day.temperatureMax,
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
