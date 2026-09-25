package com.igorwojda.showcase.domain.repository

import com.igorwojda.showcase.domain.model.LocationModel

/**
 * Location of the device, read through the platform API (`LocationRepositoryImpl` in each platform source set).
 *
 * Callers need the location permission; the app grants it before the forecast is shown.
 */
internal interface LocationRepository {
    /** The current (approximate) location. Throws when the location is unavailable or the permission is missing. */
    suspend fun getCurrentLocation(): LocationModel
}
