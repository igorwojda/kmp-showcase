package com.igorwojda.showcase.feature.base.di

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

/**
 * Starts the single Koin container: [baseModule] plus the [featureModules] the app ships.
 *
 * Only the composition roots call it (the Android app and `:iosBridge`). They depend on the features,
 * so they pass the features' modules in, and no feature module has to know about the others.
 * Platforms pass their own [config] (Android adds the context and logger).
 *
 * Hidden from Swift, so Swift only sees the `:iosBridge` `initializeKoin()`.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun initializeKoin(featureModules: List<Module>, config: KoinAppDeclaration? = null) {
    startKoin {
        includes(config)
        modules(listOf(baseModule) + featureModules)
    }
}
