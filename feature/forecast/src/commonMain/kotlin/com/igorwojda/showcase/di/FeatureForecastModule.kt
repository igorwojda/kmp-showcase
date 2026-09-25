package com.igorwojda.showcase.di

import com.igorwojda.showcase.data.repository.ForecastRepositoryImpl
import com.igorwojda.showcase.domain.repository.ForecastRepository
import com.igorwojda.showcase.domain.usecase.GetDailyWeatherUseCase
import com.igorwojda.showcase.domain.usecase.GetForecastUseCase
import com.igorwojda.showcase.presentation.dailyforecast.DailyForecastViewModel
import com.igorwojda.showcase.presentation.weeklyforecast.WeeklyForecastViewModel
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
