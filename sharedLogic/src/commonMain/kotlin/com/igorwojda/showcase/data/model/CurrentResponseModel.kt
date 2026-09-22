package com.igorwojda.showcase.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CurrentResponseModel(
    @SerialName("time")
    val time: String,
    @SerialName("temperature_2m")
    val temperature: Double,
    @SerialName("wind_speed_10m")
    val windSpeed: Double,
    @SerialName("weather_code")
    val weatherCode: Int,
)
