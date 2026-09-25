package com.igorwojda.showcase.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.CancellationSignal
import com.igorwojda.showcase.domain.model.LocationModel
import com.igorwojda.showcase.domain.repository.LocationRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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

        // Without a fix (e.g. indoors, provider timeout) the last known location is still good enough for weather.
        val location =
            checkNotNull(requestCurrentLocation(provider) ?: locationManager.getLastKnownLocation(provider)) {
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

    private companion object {
        /** In order of preference. With the coarse permission Android fuzzes the location of every provider. */
        val PROVIDERS =
            listOf(
                LocationManager.FUSED_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.GPS_PROVIDER,
            )
    }
}
