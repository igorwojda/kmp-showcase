package com.igorwojda.showcase

import android.app.Application
import com.igorwojda.showcase.di.featureForecastModule
import com.igorwojda.showcase.feature.base.di.initializeKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class KMPShowcaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // iOS Koin modules are listed in the iosBridge module.
        val androidModules =
            listOf(
                featureForecastModule,
            )

        initializeKoin(androidModules) {
            androidLogger()
            androidContext(this@KMPShowcaseApplication)
        }
    }
}
