package com.igorwojda.showcase.feature.permission.di

import com.igorwojda.showcase.feature.permission.presentation.location.LocationPermissionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Presentation dependencies of the permission feature, shared by Android and iOS. */
val featurePermissionModule = module {
    viewModelOf(::LocationPermissionViewModel)
}
