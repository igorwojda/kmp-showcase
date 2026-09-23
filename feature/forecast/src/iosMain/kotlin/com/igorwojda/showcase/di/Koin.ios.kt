package com.igorwojda.showcase.di

import com.igorwojda.showcase.presentation.forecastday.ForecastDayViewModel
import com.igorwojda.showcase.presentation.forecast.ForecastViewModel
import kotlinx.datetime.LocalDate
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform

// TODO: Another way to provide instance? Perhaps delegate exists?
/** Swift can't use Koin's reified `get()`, so every resolved type needs an explicit accessor. */
fun provideForecastViewModel(): ForecastViewModel = KoinPlatform.getKoin().get()

fun provideForecastDayViewModel(date: LocalDate): ForecastDayViewModel =
    KoinPlatform.getKoin().get { parametersOf(date) }
