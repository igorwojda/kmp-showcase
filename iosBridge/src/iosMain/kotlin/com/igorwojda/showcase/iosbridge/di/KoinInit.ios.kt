package com.igorwojda.showcase.iosbridge.di

import com.igorwojda.showcase.feature.base.di.initializeKoin
import com.igorwojda.showcase.feature.forecast.di.featureForecastModule

/** Starts Koin for the iOS app with the modules of every feature in this framework. Called from Swift. */
fun initializeKoin() {
    // Android Koin modules are listed in the KMPShowcaseApplication class.
    val iOSModules =
        listOf(
            featureForecastModule,
        )

    initializeKoin(iOSModules)
}
