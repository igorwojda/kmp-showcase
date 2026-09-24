package com.igorwojda.showcase.feature.permission.presentation.location

import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveDeniedLocationPermissionTest {
    @Test
    fun `first denial can be asked again`() {
        assertEquals(LocationPermissionStatus.Denied, resolve(rationaleBefore = false, rationaleAfter = true))
    }

    @Test
    fun `dismissing the dialog after a denial keeps it denied`() {
        assertEquals(LocationPermissionStatus.Denied, resolve(rationaleBefore = true, rationaleAfter = true))
    }

    @Test
    fun `second denial is permanent`() {
        assertEquals(
            LocationPermissionStatus.PermanentlyDenied,
            resolve(rationaleBefore = true, rationaleAfter = false, wasDenied = true),
        )
    }

    @Test
    fun `first dismissal without an answer is not a denial`() {
        assertEquals(LocationPermissionStatus.NotDetermined, resolve(rationaleBefore = false, rationaleAfter = false))
    }

    @Test
    fun `no dialog after an earlier denial is permanent`() {
        assertEquals(
            LocationPermissionStatus.PermanentlyDenied,
            resolve(rationaleBefore = false, rationaleAfter = false, wasDenied = true),
        )
    }

    @Test
    fun `answer without the dialog is permanent`() {
        assertEquals(
            LocationPermissionStatus.PermanentlyDenied,
            resolve(rationaleBefore = false, rationaleAfter = false, wasAnsweredWithoutDialog = true),
        )
    }

    @Test
    fun `repeated dismissal is treated as permanent`() {
        assertEquals(
            LocationPermissionStatus.PermanentlyDenied,
            resolve(rationaleBefore = false, rationaleAfter = false, wasDismissed = true),
        )
    }

    private fun resolve(
        rationaleBefore: Boolean,
        rationaleAfter: Boolean,
        wasAnsweredWithoutDialog: Boolean = false,
        wasDenied: Boolean = false,
        wasDismissed: Boolean = false,
    ) = resolveDeniedLocationPermission(rationaleBefore, rationaleAfter, wasAnsweredWithoutDialog, wasDenied, wasDismissed)
}
