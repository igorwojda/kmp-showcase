package com.igorwojda.showcase.domain.model

data class DailyWeatherModel(
    /** ISO-8601 local date, e.g. `2026-09-22`. */
    val date: String,
    val temperatureMin: Double,
    val temperatureMax: Double,
    val temperatureUnit: String,
    val weatherCode: Int,
)
