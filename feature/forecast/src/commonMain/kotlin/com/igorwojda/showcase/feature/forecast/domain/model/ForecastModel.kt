package com.igorwojda.showcase.feature.forecast.domain.model

/** Weather forecast for a single location. */
data class ForecastModel(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeatherModel,
    val daily: List<DailyWeatherModel>,
)
