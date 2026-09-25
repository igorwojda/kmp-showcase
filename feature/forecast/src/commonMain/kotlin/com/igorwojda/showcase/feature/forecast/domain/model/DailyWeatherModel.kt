package com.igorwojda.showcase.feature.forecast.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class DailyWeatherModel(
    /** Local date of the forecast, in the location's own time zone. */
    val date: LocalDate,
    val temperatureMin: Double,
    val temperatureMax: Double,
    val temperatureUnit: String,
    val weatherCode: Int,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    val precipitationSum: Double,
    val precipitationUnit: String,
    /** Maximum precipitation probability in percent; `null` when the model has no data for the day. */
    val precipitationProbabilityMax: Int?,
    val windSpeedMax: Double,
    val windSpeedUnit: String,
    /** Temperature for each hour of [date], in [temperatureUnit]. */
    val hourlyTemperatures: List<HourlyTemperatureModel>,
)
