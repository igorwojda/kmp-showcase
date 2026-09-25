package com.igorwojda.showcase.data.repository

import com.igorwojda.showcase.domain.model.LocationModel
import com.igorwojda.showcase.domain.repository.LocationRepository
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLErrorDomain
import platform.CoreLocation.kCLErrorLocationUnknown
import platform.CoreLocation.kCLLocationAccuracyKilometer
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSinceNow
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** Reads the location with `CLLocationManager.requestLocation()`. */
@OptIn(ExperimentalForeignApi::class)
internal class LocationRepositoryImpl : LocationRepository {
    // CLLocationManager delivers callbacks on the run loop of the thread that created it, so it's created on main.
    override suspend fun getCurrentLocation(): LocationModel =
        withContext(Dispatchers.Main) {
            // A recent last known location is good enough for weather. A fresh request is capped and falls back to
            // the last known one, e.g. when the device (or a simulator with no simulated location) can't get a fix.
            val lastKnownLocation = CLLocationManager().location
            val location =
                checkNotNull(
                    lastKnownLocation?.takeIf { it.age < MAX_LAST_KNOWN_LOCATION_AGE }
                        ?: withTimeoutOrNull(CURRENT_LOCATION_TIMEOUT) { requestCurrentLocation() }
                        ?: lastKnownLocation,
                ) {
                    "Current location is unavailable"
                }

            location.coordinate.useContents { LocationModel(latitude = latitude, longitude = longitude) }
        }

    /** A fresh location, or `null` when Core Location can't get a fix. */
    private suspend fun requestCurrentLocation() =
        suspendCancellableCoroutine { continuation ->
            val request = LocationRequest(continuation)
            // CLLocationManager holds its delegate weakly; this handler keeps the request alive until it's answered.
            continuation.invokeOnCancellation { request.cancel() }
            request.start()
        }

    private val CLLocation.age: Duration
        get() = (-timestamp.timeIntervalSinceNow).seconds

    private companion object {
        val MAX_LAST_KNOWN_LOCATION_AGE = 30.minutes
        val CURRENT_LOCATION_TIMEOUT = 5.seconds
    }
}

/** One `requestLocation()` call: [start] asks for the location, the delegate callbacks resume [continuation]. */
@OptIn(ExperimentalForeignApi::class)
private class LocationRequest(
    private val continuation: CancellableContinuation<CLLocation?>,
) : NSObject(),
    CLLocationManagerDelegateProtocol {
    private val manager = CLLocationManager()

    fun start() {
        manager.delegate = this
        // Approximate location is enough for weather, and it's answered faster.
        manager.desiredAccuracy = kCLLocationAccuracyKilometer
        manager.requestLocation()
    }

    fun cancel() {
        manager.stopUpdatingLocation()
        manager.delegate = null
    }

    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>,
    ) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return
        manager.delegate = null
        if (continuation.isActive) continuation.resume(location)
    }

    override fun locationManager(
        manager: CLLocationManager,
        didFailWithError: NSError,
    ) {
        manager.delegate = null
        if (!continuation.isActive) return
        // "Location unknown" only means there's no fix; other errors (e.g. denied permission) are real failures.
        if (didFailWithError.domain == kCLErrorDomain && didFailWithError.code == kCLErrorLocationUnknown) {
            continuation.resume(null)
        } else {
            val message = "Current location is unavailable: ${didFailWithError.localizedDescription}"
            continuation.resumeWithException(IllegalStateException(message))
        }
    }
}
