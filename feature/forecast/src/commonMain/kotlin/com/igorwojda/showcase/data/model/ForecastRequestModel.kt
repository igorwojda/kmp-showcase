package com.igorwojda.showcase.data.model

import io.ktor.resources.Resource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Open-Meteo forecast request. Every property is sent as a query parameter (lists as repeated keys, e.g.
 * `daily=sunrise&daily=sunset`).
 */
@Serializable
@Resource("/v1/forecast")
internal data class ForecastRequestModel(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("forecast_days")
    val forecastDays: Int,
    @SerialName("current")
    val current: List<String> = listOf("temperature_2m", "wind_speed_10m", "weather_code"),
    @SerialName("daily")
    val daily: List<String> =
        listOf(
            "temperature_2m_min",
            "temperature_2m_max",
            "weather_code",
            "sunrise",
            "sunset",
            "precipitation_sum",
            "precipitation_probability_max",
            "wind_speed_10m_max",
        ),
    @SerialName("hourly")
    val hourly: List<String> = listOf("temperature_2m"),
    @SerialName("timezone")
    val timezone: String = "auto",
)
