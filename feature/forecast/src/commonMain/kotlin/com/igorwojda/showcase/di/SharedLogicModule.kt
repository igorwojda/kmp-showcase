package com.igorwojda.showcase.di

import com.igorwojda.showcase.data.ForecastRepository
import com.igorwojda.showcase.presentation.forecast.ForecastViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Data, domain and presentation dependencies shared by Android and iOS. */
val sharedLogicModule = module {
    singleOf(::ForecastRepository)
    viewModelOf(::ForecastViewModel)
}
