package com.igorwojda.showcase.feature.forecast.presentation.weeklyforecast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Cold-to-warm gradient used by the daily temperature range bars. */
private val temperatureGradient = listOf(Color(0xFF4FC3F7), Color(0xFFFFB74D))

/** A single-degree day would otherwise collapse to an invisible bar. */
private const val MIN_FILL_FRACTION = 0.05f

/**
 * Horizontal bar showing where [low]..[high] sits inside the [scaleMin]..[scaleMax] range
 * shared by every day of the forecast.
 */
@Composable
internal fun TemperatureRangeBar(
    low: Double,
    high: Double,
    scaleMin: Double,
    scaleMax: Double,
    modifier: Modifier = Modifier,
) {
    val scaleSpan = (scaleMax - scaleMin).takeIf { it > 0.0 } ?: 1.0
    val startFraction = ((low - scaleMin) / scaleSpan).toFloat().coerceIn(0f, 1f)
    val endFraction = ((high - scaleMin) / scaleSpan).toFloat().coerceIn(startFraction, 1f)
    val fillFraction = (endFraction - startFraction).coerceAtLeast(MIN_FILL_FRACTION)
    val leadingFraction = startFraction.coerceAtMost(1f - fillFraction)
    val trailingFraction = 1f - leadingFraction - fillFraction

    Row(
        modifier =
            modifier
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (leadingFraction > 0f) {
            Spacer(modifier = Modifier.weight(leadingFraction))
        }
        Box(
            modifier =
                Modifier
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
