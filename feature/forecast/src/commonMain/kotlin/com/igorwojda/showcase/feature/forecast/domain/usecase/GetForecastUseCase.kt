package com.igorwojda.showcase.feature.forecast.domain.usecase

import com.igorwojda.showcase.feature.forecast.domain.model.ForecastModel
import com.igorwojda.showcase.feature.forecast.domain.repository.ForecastRepository

/** Current weather plus a daily forecast for the coming days. See [ForecastRepository.getForecast]. */
internal class GetForecastUseCase(
    private val forecastRepository: ForecastRepository,
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): ForecastModel = forecastRepository.getForecast(forceRefresh)
}
