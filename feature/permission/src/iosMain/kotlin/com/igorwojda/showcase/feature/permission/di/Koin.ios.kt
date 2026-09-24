package com.igorwojda.showcase.feature.permission.di

import com.igorwojda.showcase.feature.permission.presentation.location.LocationPermissionViewModel
import org.koin.mp.KoinPlatform

/** Swift can't use Koin's reified `get()`, so every resolved type needs an explicit accessor. */
fun provideLocationPermissionViewModel(): LocationPermissionViewModel = KoinPlatform.getKoin().get()
