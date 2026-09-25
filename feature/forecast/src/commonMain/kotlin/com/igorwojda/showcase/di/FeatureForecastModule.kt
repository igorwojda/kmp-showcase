package com.igorwojda.showcase.di

import com.igorwojda.showcase.data.repository.ForecastRepositoryImpl
import com.igorwojda.showcase.domain.repository.ForecastRepository
import com.igorwojda.showcase.presentation.dailyforecast.DailyForecastViewModel
import com.igorwojda.showcase.presentation.weeklyforecast.WeeklyForecastViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Data, domain and presentation dependencies of the forecast feature, shared by Android and iOS. */
val featureForecastModule =
    module {
        includes(featureForecastPlatformModule)

        // Data layer
        singleOf(::ForecastRepositoryImpl) bind ForecastRepository::class

        // Presentation layer
        viewModelOf(::WeeklyForecastViewModel)
        viewModelOf(::DailyForecastViewModel)
    }

/** Dependencies implemented with platform APIs, e.g. the device location. */
internal expect val featureForecastPlatformModule: Module
