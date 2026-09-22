package com.igorwojda.showcase.domain.model

import kotlinx.datetime.LocalDateTime

data class CurrentWeatherModel(
    val time: LocalDateTime,
    val temperature: Double,
    val temperatureUnit: String,
    val windSpeed: Double,
    val windSpeedUnit: String,
    val weatherCode: Int,
)
