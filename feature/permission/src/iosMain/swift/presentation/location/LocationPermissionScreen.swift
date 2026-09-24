import SwiftUI
import KMPObservableViewModelSwiftUI
import iosBridge

struct LocationPermissionScreen: View {
    let onPermissionGrant: () -> Void

    @StateViewModel private var viewModel = provideLocationPermissionViewModel()
    @State private var authorization = LocationAuthorization()

    var body: some View {
        // SKIE turns `states` into an AsyncSequence, so SwiftUI can observe the store directly.
        Observing(viewModel.states) { state in
            LocationPermissionContent(
                state: state,
                onAllow: { viewModel.onIntent(intent: LocationPermissionIntentAllow.shared) },
                onOpenSettings: { viewModel.onIntent(intent: LocationPermissionIntentOpenSettings.shared) }
            )
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        // `.task` runs while the screen is visible, so the store sees the subscriber come and go.
        .task {
            for await action in viewModel.actions {
                switch onEnum(of: action) {
                case .launchPermissionRequest:
                    authorization.request()
                case .launchSettings:
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        _ = await UIApplication.shared.open(url)
                    }
                case .permissionGranted:
                    onPermissionGrant()
                }
            }
        }
        // Reports the first status, and every change, e.g. after the user allows it in Settings.
        .task {
            for await status in authorization.statuses {
                viewModel.onIntent(intent: LocationPermissionIntentStatusChanged(status: status))
            }
        }
    }
}

private struct LocationPermissionContent: View {
    let state: LocationPermissionState
    let onAllow: () -> Void
    let onOpenSettings: () -> Void

    var body: some View {
        // `onEnum(of:)` makes the sealed interface exhaustive – a new state stops compiling here.
        switch onEnum(of: state) {
        case .checking:
            Color.clear
        case .notDetermined:
            PermissionMessage(
                message: "KMP Showcase uses your approximate location to show the weather where you are.",
                buttonTitle: "Allow location access",
                action: onAllow
            )
        case .denied:
            PermissionMessage(
                message: "Without location access KMP Showcase can't show the weather where you are.",
                buttonTitle: "Try again",
                action: onAllow
            )
        case .permanentlyDenied:
            PermissionMessage(
                message: "Location access is turned off for KMP Showcase. Allow it in Settings to continue.",
                buttonTitle: "Open Settings",
                action: onOpenSettings
            )
        case .servicesDisabled:
            PermissionMessage(
                message: "Location Services are turned off. Turn them on in Settings › Privacy & Security › Location Services.",
                buttonTitle: "Open Settings",
                action: onOpenSettings
            )
        case .restricted:
            PermissionMessage(
                message: "Location access is restricted on this device, e.g. by parental controls or device management."
            )
        }
    }
}

/// An explanation plus an optional button; the button is hidden when the user can't act.
private struct PermissionMessage: View {
    let message: String
    var buttonTitle: String?
    var action: () -> Void = {}

    var body: some View {
        VStack(spacing: 8) {
            Text("📍")
                .font(.system(size: 40))
            Text(message)
                .multilineTextAlignment(.center)
            if let buttonTitle {
                Button(buttonTitle, action: action)
                    .buttonStyle(.borderedProminent)
            }
        }
        .padding(24)
    }
}

#Preview("Not determined") {
    LocationPermissionContent(state: LocationPermissionStateNotDetermined.shared, onAllow: {}, onOpenSettings: {})
}

#Preview("Denied") {
    LocationPermissionContent(state: LocationPermissionStateDenied.shared, onAllow: {}, onOpenSettings: {})
}

#Preview("Permanently denied") {
    LocationPermissionContent(state: LocationPermissionStatePermanentlyDenied.shared, onAllow: {}, onOpenSettings: {})
}

#Preview("Services disabled") {
    LocationPermissionContent(state: LocationPermissionStateServicesDisabled.shared, onAllow: {}, onOpenSettings: {})
}

#Preview("Restricted") {
    LocationPermissionContent(state: LocationPermissionStateRestricted.shared, onAllow: {}, onOpenSettings: {})
}
