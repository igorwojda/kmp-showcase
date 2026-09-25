package com.igorwojda.showcase.feature.forecast.domain.repository

import com.igorwojda.showcase.feature.forecast.domain.model.DailyWeatherModel
import com.igorwojda.showcase.feature.forecast.domain.model.ForecastModel
import kotlinx.datetime.LocalDate

/** Weather forecast for a single location. */
internal interface ForecastRepository {
    /**
     * Current weather plus a daily forecast for the coming days.
     *
     * May return a cached forecast, unless [forceRefresh] is set. Throws on network / parsing failure.
     */
    suspend fun getForecast(forceRefresh: Boolean = false): ForecastModel

    /**
     * Weather for a single [date] of the (possibly cached) forecast, or `null` when the forecast doesn't contain [date].
     *
     * Throws on network / parsing failure.
     */
    suspend fun getDailyWeather(date: LocalDate): DailyWeatherModel?
}
