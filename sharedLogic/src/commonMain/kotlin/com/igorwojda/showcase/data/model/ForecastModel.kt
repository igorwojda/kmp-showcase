package com.igorwojda.showcase.data.model

/** Weather forecast for a single location. */
data class ForecastModel(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeatherModel,
    val daily: List<DailyWeatherModel>,
)
