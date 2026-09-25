package com.igorwojda.showcase.feature.permission.presentation.location

/** Resolves the status after the system dialog returned "not granted". */
internal class DeniedLocationPermissionResolver {
    /**
     * - [rationaleAfter]: the user denied, and the dialog can be shown again.
     * - [rationaleBefore] without [rationaleAfter]: the second denial, which is permanent.
     * - No rationale before or after: the dialog was dismissed, or it wasn't shown because the permission is already
     *   permanently denied. It's the latter when the answer came too fast for a person ([wasAnsweredWithoutDialog]) or
     *   after an earlier denial ([wasDenied]). A repeated dismissal ([wasDismissed]) is treated as permanent too, so
     *   the user is never stuck on a button that does nothing.
     */
    fun resolve(
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
}
