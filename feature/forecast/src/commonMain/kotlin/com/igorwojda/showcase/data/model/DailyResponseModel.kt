package com.igorwojda.showcase.data.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DailyResponseModel(
    @SerialName("time")
    val time: List<LocalDate>,
    @SerialName("temperature_2m_min")
    val temperatureMin: List<Double>,
    @SerialName("temperature_2m_max")
    val temperatureMax: List<Double>,
    @SerialName("weather_code")
    val weatherCode: List<Int>,
    @SerialName("sunrise")
    val sunrise: List<LocalDateTime>,
    @SerialName("sunset")
    val sunset: List<LocalDateTime>,
    @SerialName("precipitation_sum")
    val precipitationSum: List<Double>,
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int?>,
    @SerialName("wind_speed_10m_max")
    val windSpeedMax: List<Double>,
)
