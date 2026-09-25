package com.igorwojda.showcase.domain.repository

import com.igorwojda.showcase.domain.model.DailyWeatherModel
import com.igorwojda.showcase.domain.model.ForecastModel
import kotlinx.datetime.LocalDate

/** Weather forecast for the device location. */
internal interface ForecastRepository {
    /**
     * Current weather plus a daily forecast for the coming days.
     *
     * May return a cached forecast, unless [forceRefresh] is set. Throws on location / network / parsing failure.
     */
    suspend fun getForecast(forceRefresh: Boolean = false): ForecastModel

    /**
     * Weather for a single [date] of the (possibly cached) forecast, or `null` when the forecast doesn't contain [date].
     *
     * Throws on location / network / parsing failure.
     */
    suspend fun getDailyWeather(date: LocalDate): DailyWeatherModel?
}
