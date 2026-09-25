package com.igorwojda.showcase.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.location.Location
import android.os.CancellationSignal
import android.os.SystemClock
import com.igorwojda.showcase.domain.model.LocationModel
import com.igorwojda.showcase.domain.repository.LocationRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

/** Reads the location with the platform [LocationManager], so no Google Play services are needed. */
internal class LocationRepositoryImpl(
    private val context: Context,
) : LocationRepository {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    override suspend fun getCurrentLocation(): LocationModel {
        check(context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            "Location permission is not granted"
        }

        val provider =
            checkNotNull(PROVIDERS.firstOrNull { locationManager.isProviderEnabled(it) }) {
                "Location is turned off on this device"
            }

        // A recent last known location is good enough for weather. Some providers never get a fresh fix and only
        // give up after their own ~30 s timeout, so the fresh request is capped and falls back to the last known one.
        val lastKnownLocation = locationManager.getLastKnownLocation(provider)
        val location =
            checkNotNull(
                lastKnownLocation?.takeIf { it.age < MAX_LAST_KNOWN_LOCATION_AGE }
                    ?: withTimeoutOrNull(CURRENT_LOCATION_TIMEOUT) { requestCurrentLocation(provider) }
                    ?: lastKnownLocation,
            ) {
                "Current location is unavailable"
            }

        return LocationModel(latitude = location.latitude, longitude = location.longitude)
    }

    /** A fresh location, or `null` when the provider gives up (it times out on its own). */
    private suspend fun requestCurrentLocation(provider: String) =
        suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            continuation.invokeOnCancellation { cancellationSignal.cancel() }

            locationManager.getCurrentLocation(provider, cancellationSignal, context.mainExecutor) { location ->
                continuation.resume(location)
            }
        }

    private val Location.age
        get() = (SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos).nanoseconds

    private companion object {
        val MAX_LAST_KNOWN_LOCATION_AGE = 30.minutes
        val CURRENT_LOCATION_TIMEOUT = 5.seconds

        /** In order of preference. With the coarse permission Android fuzzes the location of every provider. */
        val PROVIDERS =
            listOf(
                LocationManager.FUSED_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.GPS_PROVIDER,
            )
    }
}
