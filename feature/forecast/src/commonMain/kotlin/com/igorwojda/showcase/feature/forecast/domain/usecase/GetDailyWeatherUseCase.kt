package com.igorwojda.showcase.feature.forecast.domain.usecase

import com.igorwojda.showcase.feature.forecast.domain.model.DailyWeatherModel
import com.igorwojda.showcase.feature.forecast.domain.repository.ForecastRepository
import kotlinx.datetime.LocalDate

/** Weather for a single [LocalDate], or `null` when the forecast doesn't contain it. See [ForecastRepository.getDailyWeather]. */
internal class GetDailyWeatherUseCase(
    private val forecastRepository: ForecastRepository,
) {
    suspend operator fun invoke(date: LocalDate): DailyWeatherModel? = forecastRepository.getDailyWeather(date)
}
