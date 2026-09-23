package com.igorwojda.showcase.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DailyUnitsResponseModel(
    @SerialName("temperature_2m_max")
    val temperatureMax: String,
    @SerialName("precipitation_sum")
    val precipitationSum: String,
    @SerialName("wind_speed_10m_max")
    val windSpeedMax: String,
)
