package com.igorwojda.showcase.domain.model

data class CurrentWeatherModel(
    /** ISO-8601 local time of the measurement, e.g. `2026-09-22T14:00`. */
    val time: String,
    val temperature: Double,
    val temperatureUnit: String,
    val windSpeed: Double,
    val windSpeedUnit: String,
    val weatherCode: Int,
)
