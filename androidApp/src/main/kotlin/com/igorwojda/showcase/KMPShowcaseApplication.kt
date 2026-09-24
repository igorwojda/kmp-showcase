package com.igorwojda.showcase

import android.app.Application
import com.igorwojda.showcase.di.forecastModule
import com.igorwojda.showcase.feature.base.di.initializeKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class KMPShowcaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val modules = listOf(forecastModule)

        initializeKoin(modules) {
            androidLogger()
            androidContext(this@KMPShowcaseApplication)
        }
    }
}
