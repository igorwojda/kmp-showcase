package com.igorwojda.showcase.iosbridge.di

import com.igorwojda.showcase.di.featureForecastModule
import com.igorwojda.showcase.feature.base.di.initializeKoin
import com.igorwojda.showcase.feature.permission.di.featurePermissionModule

/** Starts Koin for the iOS app with the modules of every feature in this framework. Called from Swift. */
fun initializeKoin() {
    // Android modules are defined in KMPShowcaseApplication class.
    val iOSModules = listOf(
        featureForecastModule,
        featurePermissionModule,
    )

    initializeKoin(iOSModules)
}
