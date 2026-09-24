package com.igorwojda.showcase.feature.permission.presentation.location

import android.os.SystemClock
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlin.time.Duration.Companion.milliseconds

/** Shows the system permission dialog and opens the app's Settings; created by [rememberLocationPermissionRequester]. */
internal interface LocationPermissionRequester {
    /** Shows the system dialog; the answer is reported as a status change. */
    fun request()

    fun openSettings()
}

/**
 * Talks to the Android permission APIs and reports every [LocationPermissionStatus] to [onStatusChange]: the first
 * one, the answer to each [request][LocationPermissionRequester.request], and a re-check whenever the screen resumes,
 * e.g. after the user granted it in Settings.
 *
 * Android only answers "granted" or "not granted", so the state needed to read the answer
 * (see [LocationPermissionChecker.statusAfterRequest]) is kept here, not in the shared ViewModel.
 */
@Composable
internal fun rememberLocationPermissionRequester(onStatusChange: (LocationPermissionStatus) -> Unit): LocationPermissionRequester {
    val activity = checkNotNull(LocalActivity.current) { "The location permission must be requested from an Activity" }
    val checker = remember(activity) { LocationPermissionChecker(activity) }
    val currentOnStatusChange by rememberUpdatedState(onStatusChange)

    // Saved, so an answer that arrives after rotation or process death is still interpreted correctly.
    // `elapsedRealtime` keeps counting across process death.
    var rationaleBeforeRequest by rememberSaveable { mutableStateOf(false) }
    var requestLaunchedAt by rememberSaveable { mutableLongStateOf(0L) }
    // A second request while the dialog is open is answered "not granted" without asking; it isn't a denial.
    var isRequestPending by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            isRequestPending = false
            val answerTime = (SystemClock.elapsedRealtime() - requestLaunchedAt).milliseconds
            currentOnStatusChange(checker.statusAfterRequest(isGranted, rationaleBeforeRequest, answerTime))
        }

    LifecycleResumeEffect(checker) {
        if (!isRequestPending) {
            currentOnStatusChange(checker.currentStatus())
        }
        onPauseOrDispose {}
    }

    return remember(activity, checker, permissionLauncher) {
        object : LocationPermissionRequester {
            override fun request() {
                if (isRequestPending) return
                isRequestPending = true
                rationaleBeforeRequest = checker.shouldShowRationale()
                requestLaunchedAt = SystemClock.elapsedRealtime()
                permissionLauncher.launch(LOCATION_PERMISSION)
            }

            override fun openSettings() {
                activity.startActivity(checker.appSettingsIntent())
            }
        }
    }
}
