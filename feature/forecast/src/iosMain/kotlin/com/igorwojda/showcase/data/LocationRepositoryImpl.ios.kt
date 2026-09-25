package com.igorwojda.showcase.data

import com.igorwojda.showcase.domain.model.LocationModel
import com.igorwojda.showcase.domain.repository.LocationRepository
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyKilometer
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Reads the location with `CLLocationManager.requestLocation()`. */
internal class LocationRepositoryImpl : LocationRepository {
    // CLLocationManager delivers callbacks on the run loop of the thread that created it, so it's created on main.
    override suspend fun getCurrentLocation(): LocationModel =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val request = LocationRequest(continuation)
                // CLLocationManager holds its delegate weakly; this handler keeps the request alive until it's answered.
                continuation.invokeOnCancellation { request.cancel() }
                request.start()
            }
        }
}

/** One `requestLocation()` call: [start] asks for the location, the delegate callbacks resume [continuation]. */
@OptIn(ExperimentalForeignApi::class)
private class LocationRequest(
    private val continuation: CancellableContinuation<LocationModel>,
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
        val model = location.coordinate.useContents { LocationModel(latitude = latitude, longitude = longitude) }
        if (continuation.isActive) continuation.resume(model)
    }

    override fun locationManager(
        manager: CLLocationManager,
        didFailWithError: NSError,
    ) {
        manager.delegate = null
        if (continuation.isActive) {
            val message = "Current location is unavailable: ${didFailWithError.localizedDescription}"
            continuation.resumeWithException(IllegalStateException(message))
        }
    }
}
