package com.igorwojda.showcase.di

import com.igorwojda.showcase.feature.base.di.baseModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

/**
 * Starts the single Koin container shared by both platforms.
 *
 * Platform entry points pass their own [config] (Android adds the context and logger).
 */
fun initializeKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        includes(config)
        modules(baseModule, forecastModule)
    }
}
