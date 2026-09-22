package com.igorwojda.showcase.data.model

import kotlinx.datetime.LocalDate
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
)
