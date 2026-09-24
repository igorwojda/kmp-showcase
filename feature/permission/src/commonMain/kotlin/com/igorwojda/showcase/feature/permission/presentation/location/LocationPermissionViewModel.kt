package com.igorwojda.showcase.feature.permission.presentation.location

import com.igorwojda.showcase.feature.base.presentation.flowmvi.StoreViewModel
import com.igorwojda.showcase.feature.base.presentation.flowmvi.configuredStore
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.plugins.reduce

/**
 * Explains why the app needs the location and reacts to the permission status.
 *
 * The OS permission APIs are platform UI APIs (Android Activity Result API, iOS `CLLocationManager`), so the
 * screen talks to them: it runs [LocationPermissionAction]s and reports every status as
 * [LocationPermissionIntent.StatusChanged].
 */
class LocationPermissionViewModel :
    StoreViewModel<LocationPermissionState, LocationPermissionIntent, LocationPermissionAction>() {

    override val store = configuredStore(initial = LocationPermissionState.Checking, name = "LocationPermission") {
        reduce { intent ->
            when (intent) {
                LocationPermissionIntent.Allow -> action(LocationPermissionAction.LaunchPermissionRequest)
                LocationPermissionIntent.OpenSettings -> action(LocationPermissionAction.LaunchSettings)
                is LocationPermissionIntent.StatusChanged -> when (intent.status) {
                    LocationPermissionStatus.Granted -> action(LocationPermissionAction.PermissionGranted)
                    LocationPermissionStatus.NotDetermined -> updateState { LocationPermissionState.NotDetermined }
                    LocationPermissionStatus.Denied -> updateState { LocationPermissionState.Denied }
                    LocationPermissionStatus.PermanentlyDenied -> updateState {
                        LocationPermissionState.PermanentlyDenied
                    }
                    LocationPermissionStatus.Restricted -> updateState { LocationPermissionState.Restricted }
                    LocationPermissionStatus.ServicesDisabled -> updateState {
                        LocationPermissionState.ServicesDisabled
                    }
                }
            }
        }
    }
}

/** Location permission as reported by the platform. */
enum class LocationPermissionStatus {
    /** Never asked, or the Android dialog was dismissed without an answer. The system dialog can be shown. */
    NotDetermined,

    /** Android only: denied once. The system dialog can be shown again. */
    Denied,

    /** The system dialog won't be shown anymore; the user can grant the permission only in Settings. */
    PermanentlyDenied,

    /** Blocked by device policy or parental controls; the user can't grant it. */
    Restricted,

    /** iOS only: Location Services are turned off for the whole device. */
    ServicesDisabled,

    Granted,
}

sealed interface LocationPermissionState : MVIState {
    /** Waiting for the first status from the platform, so the screen doesn't flash the wrong message. */
    data object Checking : LocationPermissionState
    data object NotDetermined : LocationPermissionState
    data object Denied : LocationPermissionState
    data object PermanentlyDenied : LocationPermissionState
    data object Restricted : LocationPermissionState
    data object ServicesDisabled : LocationPermissionState
}

sealed interface LocationPermissionIntent : MVIIntent {
    data object Allow : LocationPermissionIntent
    data object OpenSettings : LocationPermissionIntent
    data class StatusChanged(val status: LocationPermissionStatus) : LocationPermissionIntent
}

sealed interface LocationPermissionAction : MVIAction {
    /** Show the system permission dialog. */
    data object LaunchPermissionRequest : LocationPermissionAction

    /** Open the app's page in the system Settings. */
    data object LaunchSettings : LocationPermissionAction

    /** Leave the screen; where to go is up to the platform navigation. */
    data object PermissionGranted : LocationPermissionAction
}
