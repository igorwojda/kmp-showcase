package com.igorwojda.showcase.domain.model

import kotlinx.datetime.LocalDate

data class DailyWeatherModel(
    /** Local date of the forecast, in the location's own time zone. */
    val date: LocalDate,
    val temperatureMin: Double,
    val temperatureMax: Double,
    val temperatureUnit: String,
    val weatherCode: Int,
)
