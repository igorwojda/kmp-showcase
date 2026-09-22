package com.igorwojda.showcase

import android.app.Application
import com.igorwojda.showcase.di.initializeKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class KMPShowcaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initializeKoin {
            androidLogger()
            androidContext(this@KMPShowcaseApplication)
        }
    }
}
