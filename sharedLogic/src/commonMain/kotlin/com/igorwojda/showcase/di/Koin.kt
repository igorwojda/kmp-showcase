package com.igorwojda.showcase.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Starts the single Koin container shared by both platforms.
 *
 * Platform entry points pass their own [config] (Android adds the context and logger).
 */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(sharedLogicModule)
    }
}
