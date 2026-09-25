package com.igorwojda.showcase.feature.forecast.data.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CurrentResponseModel(
    @SerialName("time")
    val time: LocalDateTime,
    @SerialName("temperature_2m")
    val temperature: Double,
    @SerialName("wind_speed_10m")
    val windSpeed: Double,
    @SerialName("weather_code")
    val weatherCode: Int,
)
