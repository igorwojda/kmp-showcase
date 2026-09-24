package com.igorwojda.showcase.feature.permission.presentation.location

import android.os.SystemClock
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.milliseconds
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun LocationPermissionScreen(
    onPermissionGranted: () -> Unit,
    viewModel: LocationPermissionViewModel = koinViewModel(),
) {
    val store = viewModel.store
    val activity = checkNotNull(LocalActivity.current) { "LocationPermissionScreen must be hosted in an Activity" }
    val checker = remember(activity) { LocationPermissionChecker(activity) }

    // Saved, so an answer that arrives after rotation or process death is still interpreted correctly.
    // `elapsedRealtime` keeps counting across process death.
    var rationaleBeforeRequest by rememberSaveable { mutableStateOf(false) }
    var requestLaunchedAt by rememberSaveable { mutableLongStateOf(0L) }
    // A second request while the dialog is open is answered "not granted" without asking; it isn't a denial.
    var isRequestPending by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        isRequestPending = false
        val answerTime = (SystemClock.elapsedRealtime() - requestLaunchedAt).milliseconds
        val status = checker.statusAfterRequest(isGranted, rationaleBeforeRequest, answerTime)
        store.intent(LocationPermissionIntent.StatusChanged(status))
    }

    // The lambda consumes MVIActions as they arrive; it only runs while the UI is visible.
    val state by store.subscribe { action ->
        when (action) {
            LocationPermissionAction.LaunchPermissionRequest -> if (!isRequestPending) {
                isRequestPending = true
                rationaleBeforeRequest = checker.shouldShowRationale()
                requestLaunchedAt = SystemClock.elapsedRealtime()
                permissionLauncher.launch(LOCATION_PERMISSION)
            }

            LocationPermissionAction.LaunchSettings -> activity.startActivity(checker.appSettingsIntent())
            LocationPermissionAction.PermissionGranted -> onPermissionGranted()
        }
    }

    // Reports the first status, and re-checks when the user comes back, e.g. after granting it in Settings.
    LifecycleResumeEffect(checker) {
        if (!isRequestPending) {
            store.intent(LocationPermissionIntent.StatusChanged(checker.currentStatus()))
        }
        onPauseOrDispose {}
    }

    Scaffold { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            LocationPermissionContent(
                state = state,
                onAllowClick = { store.intent(LocationPermissionIntent.Allow) },
                onOpenSettingsClick = { store.intent(LocationPermissionIntent.OpenSettings) },
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun LocationPermissionContent(
    state: LocationPermissionState,
    onAllowClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        LocationPermissionState.Checking -> Unit

        LocationPermissionState.NotDetermined -> PermissionMessage(
            message = "KMP Showcase uses your approximate location to show the weather where you are.",
            buttonText = "Allow location access",
            onButtonClick = onAllowClick,
            modifier = modifier,
        )

        LocationPermissionState.Denied -> PermissionMessage(
            message = "Without location access KMP Showcase can't show the weather where you are.",
            buttonText = "Try again",
            onButtonClick = onAllowClick,
            modifier = modifier,
        )

        LocationPermissionState.PermanentlyDenied -> PermissionMessage(
            message = "Location access is turned off for KMP Showcase. Allow it in Settings to continue.",
            buttonText = "Open Settings",
            onButtonClick = onOpenSettingsClick,
            modifier = modifier,
        )

        LocationPermissionState.ServicesDisabled -> PermissionMessage(
            message = "Location is turned off on this device. Turn it on in Settings to continue.",
            buttonText = "Open Settings",
            onButtonClick = onOpenSettingsClick,
            modifier = modifier,
        )

        LocationPermissionState.Restricted -> PermissionMessage(
            message = "Location access is restricted on this device, e.g. by parental controls or device management.",
            buttonText = null,
            onButtonClick = {},
            modifier = modifier,
        )
    }
}

/** An explanation plus an optional [buttonText] button; the button is hidden when the user can't act. */
@Composable
private fun PermissionMessage(
    message: String,
    buttonText: String?,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "📍", fontSize = 40.sp)
        Text(text = message, textAlign = TextAlign.Center)
        if (buttonText != null) {
            Button(onClick = onButtonClick) {
                Text(buttonText)
            }
        }
    }
}

private class LocationPermissionStateProvider : PreviewParameterProvider<LocationPermissionState> {
    override val values = sequenceOf(
        LocationPermissionState.NotDetermined,
        LocationPermissionState.Denied,
        LocationPermissionState.PermanentlyDenied,
        LocationPermissionState.ServicesDisabled,
        LocationPermissionState.Restricted,
    )
}

@Preview(showBackground = true)
@Composable
private fun LocationPermissionContentPreview(
    @PreviewParameter(LocationPermissionStateProvider::class) state: LocationPermissionState,
) {
    MaterialTheme {
        LocationPermissionContent(state = state, onAllowClick = {}, onOpenSettingsClick = {})
    }
}
