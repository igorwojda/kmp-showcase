package com.igorwojda.showcase.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ForecastResponseModel(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("current")
    val current: CurrentResponseModel,
    @SerialName("current_units")
    val currentUnits: CurrentUnitsResponseModel,
    @SerialName("daily")
    val daily: DailyResponseModel,
    @SerialName("daily_units")
    val dailyUnits: DailyUnitsResponseModel,
    @SerialName("hourly")
    val hourly: HourlyResponseModel,
)
