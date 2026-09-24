package com.igorwojda.showcase.feature.permission.presentation.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal const val LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION

fun Context.isLocationPermissionGranted(): Boolean =
    ContextCompat.checkSelfPermission(this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED

/**
 * Maps the Android permission APIs to [LocationPermissionStatus].
 *
 * Android reports "never asked", "dialog dismissed" and "permanently denied" the same way (not granted, no
 * rationale), so the checker remembers earlier answers in [SharedPreferences][android.content.SharedPreferences].
 */
internal class LocationPermissionChecker(
    private val activity: Activity,
) {
    private val preferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /** The status without asking the user, e.g. when the screen opens or the user returns from Settings. */
    fun currentStatus(): LocationPermissionStatus =
        when {
            activity.isLocationPermissionGranted() -> LocationPermissionStatus.Granted.also { record(it) }

            isRevokedByPolicy() -> LocationPermissionStatus.Restricted

            shouldShowRationale() -> LocationPermissionStatus.Denied

            // No rationale after an earlier denial: the system won't show the dialog anymore.
            preferences.getBoolean(KEY_WAS_DENIED, false) -> LocationPermissionStatus.PermanentlyDenied

            else -> LocationPermissionStatus.NotDetermined
        }

    /** Read right before the request; [statusAfterRequest] compares it with the value after the answer. */
    fun shouldShowRationale(): Boolean = ActivityCompat.shouldShowRequestPermissionRationale(activity, LOCATION_PERMISSION)

    /** The status after the request answered [isGranted], [answerTime] after it was launched. */
    fun statusAfterRequest(
        isGranted: Boolean,
        rationaleBefore: Boolean,
        answerTime: Duration,
    ): LocationPermissionStatus =
        when {
            isGranted -> {
                LocationPermissionStatus.Granted
            }

            isRevokedByPolicy() -> {
                LocationPermissionStatus.Restricted
            }

            else -> {
                resolveDeniedLocationPermission(
                    rationaleBefore = rationaleBefore,
                    rationaleAfter = shouldShowRationale(),
                    wasAnsweredWithoutDialog = answerTime < DIALOG_MIN_ANSWER_TIME,
                    wasDenied = preferences.getBoolean(KEY_WAS_DENIED, false),
                    wasDismissed = preferences.getBoolean(KEY_WAS_DISMISSED, false),
                )
            }
        }.also { record(it) }

    fun appSettingsIntent(): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.packageName, null))

    private fun isRevokedByPolicy(): Boolean =
        activity.packageManager.isPermissionRevokedByPolicy(LOCATION_PERMISSION, activity.packageName)

    private fun record(status: LocationPermissionStatus) {
        val editor = preferences.edit()
        when (status) {
            LocationPermissionStatus.Granted -> editor.clear()

            LocationPermissionStatus.Denied,
            LocationPermissionStatus.PermanentlyDenied,
            -> editor.putBoolean(KEY_WAS_DENIED, true)

            LocationPermissionStatus.NotDetermined -> editor.putBoolean(KEY_WAS_DISMISSED, true)

            LocationPermissionStatus.Restricted,
            LocationPermissionStatus.ServicesDisabled,
            -> Unit
        }
        editor.apply()
    }

    private companion object {
        /**
         * Faster than a person can see the dialog and dismiss it, so an answer this fast means the system didn't
         * show the dialog. E.g. the permission was denied in Settings, which the stored flags don't know about.
         */
        val DIALOG_MIN_ANSWER_TIME = 500.milliseconds

        const val PREFERENCES_NAME = "location_permission"
        const val KEY_WAS_DENIED = "was_denied"
        const val KEY_WAS_DISMISSED = "was_dismissed"
    }
}

/**
 * The status after the system dialog returned "not granted".
 *
 * - [rationaleAfter]: the user denied, and the dialog can be shown again.
 * - [rationaleBefore] without [rationaleAfter]: the second denial, which is permanent.
 * - No rationale before or after: the dialog was dismissed, or it wasn't shown because the permission is already
 *   permanently denied. It's the latter when the answer came too fast for a person ([wasAnsweredWithoutDialog]) or
 *   after an earlier denial ([wasDenied]). A repeated dismissal ([wasDismissed]) is treated as permanent too, so
 *   the user is never stuck on a button that does nothing.
 */
internal fun resolveDeniedLocationPermission(
    rationaleBefore: Boolean,
    rationaleAfter: Boolean,
    wasAnsweredWithoutDialog: Boolean,
    wasDenied: Boolean,
    wasDismissed: Boolean,
): LocationPermissionStatus =
    when {
        rationaleAfter -> LocationPermissionStatus.Denied
        rationaleBefore -> LocationPermissionStatus.PermanentlyDenied
        wasAnsweredWithoutDialog || wasDenied || wasDismissed -> LocationPermissionStatus.PermanentlyDenied
        else -> LocationPermissionStatus.NotDetermined
    }
