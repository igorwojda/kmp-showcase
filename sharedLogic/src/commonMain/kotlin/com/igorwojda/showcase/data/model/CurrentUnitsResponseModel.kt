package com.igorwojda.showcase.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CurrentUnitsResponseModel(
    @SerialName("temperature_2m")
    val temperature: String,
    @SerialName("wind_speed_10m")
    val windSpeed: String,
)
