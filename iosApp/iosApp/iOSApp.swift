import SwiftUI
import iosBridge

@main
struct iOSApp: App {
    @State private var locationAuthorization = LocationAuthorization()
    @State private var isLocationPermissionGranted = LocationAuthorization.isGranted

    init() {
        initializeKoin()
    }

    var body: some Scene {
        WindowGroup {
            Group {
                // The forecast needs the location permission.
                if isLocationPermissionGranted {
                    WeeklyForecastScreen()
                } else {
                    LocationPermissionScreen { isLocationPermissionGranted = true }
                }
            }
            // The permission can change while the app runs: in Settings, or when "Allow Once" expires.
            .task {
                for await status in locationAuthorization.statuses {
                    isLocationPermissionGranted = status == .granted
                }
            }
        }
    }
}
