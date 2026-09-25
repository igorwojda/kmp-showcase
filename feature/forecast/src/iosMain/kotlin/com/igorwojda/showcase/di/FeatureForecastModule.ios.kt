package com.igorwojda.showcase.di

import com.igorwojda.showcase.data.repository.LocationRepositoryImpl
import com.igorwojda.showcase.domain.repository.LocationRepository
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val featureForecastPlatformModule: Module =
    module {
        single<LocationRepository> { LocationRepositoryImpl() }
    }
