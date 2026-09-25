package com.igorwojda.showcase.iosbridge.di

import com.igorwojda.showcase.di.featureForecastModule
import com.igorwojda.showcase.feature.base.di.initializeKoin

/** Starts Koin for the iOS app with the modules of every feature in this framework. Called from Swift. */
fun initializeKoin() {
    // Android Koin modules are listed in the KMPShowcaseApplication class.
    val iOSModules =
        listOf(
            featureForecastModule,
        )

    initializeKoin(iOSModules)
}
