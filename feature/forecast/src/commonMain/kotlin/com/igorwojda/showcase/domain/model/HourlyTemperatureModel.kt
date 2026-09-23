package com.igorwojda.showcase.domain.model

import kotlinx.datetime.LocalDateTime

data class HourlyTemperatureModel(
    /** Local time, in the location's own time zone. */
    val time: LocalDateTime,
    val temperature: Double,
)
