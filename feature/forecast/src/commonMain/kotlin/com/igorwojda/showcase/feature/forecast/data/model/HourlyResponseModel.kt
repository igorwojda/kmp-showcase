package com.igorwojda.showcase.feature.forecast.data.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HourlyResponseModel(
    @SerialName("time")
    val time: List<LocalDateTime>,
    @SerialName("temperature_2m")
    val temperature: List<Double>,
)
