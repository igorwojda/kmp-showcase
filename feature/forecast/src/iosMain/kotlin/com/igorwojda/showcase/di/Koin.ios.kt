package com.igorwojda.showcase.di

import com.igorwojda.showcase.presentation.dailyforecast.DailyForecastViewModel
import com.igorwojda.showcase.presentation.weeklyforecast.WeeklyForecastViewModel
import kotlinx.datetime.LocalDate
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform

/** Swift can't use Koin's reified `get()`, so every resolved type needs an explicit accessor. */
fun provideWeeklyForecastViewModel(): WeeklyForecastViewModel = KoinPlatform.getKoin().get()

fun provideDailyForecastViewModel(date: LocalDate): DailyForecastViewModel =
    KoinPlatform.getKoin().get { parametersOf(date) }
