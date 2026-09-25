package com.igorwojda.showcase.feature.forecast.di

import com.igorwojda.showcase.feature.forecast.data.repository.ForecastRepositoryImpl
import com.igorwojda.showcase.feature.forecast.domain.repository.ForecastRepository
import com.igorwojda.showcase.feature.forecast.domain.usecase.GetDailyWeatherUseCase
import com.igorwojda.showcase.feature.forecast.domain.usecase.GetForecastUseCase
import com.igorwojda.showcase.feature.forecast.presentation.dailyforecast.DailyForecastViewModel
import com.igorwojda.showcase.feature.forecast.presentation.weeklyforecast.WeeklyForecastViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Data, domain and presentation dependencies of the forecast feature, shared by Android and iOS. */
val featureForecastModule =
    module {
        // Data layer
        singleOf(::ForecastRepositoryImpl) bind ForecastRepository::class

        // Domain layer
        factoryOf(::GetForecastUseCase)
        factoryOf(::GetDailyWeatherUseCase)

        // Presentation layer
        viewModelOf(::WeeklyForecastViewModel)
        viewModelOf(::DailyForecastViewModel)
    }
