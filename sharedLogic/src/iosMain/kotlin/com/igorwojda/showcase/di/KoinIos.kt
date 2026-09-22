package com.igorwojda.showcase.di

import com.igorwojda.showcase.presentation.forecast.ForecastViewModel
import org.koin.mp.KoinPlatform

/** Called from `iOSApp.init()` – Swift can't use the default argument of [initKoin]. */
fun initKoinIos() = initKoin()

/** Swift can't use Koin's reified `get()`, so every resolved type needs an explicit accessor. */
fun forecastViewModel(): ForecastViewModel = KoinPlatform.getKoin().get()
