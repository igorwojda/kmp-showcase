import CoreLocation
import iosBridge

/// Maps `CLLocationManager` authorization to the shared `LocationPermissionStatus`.
final class LocationAuthorization: NSObject, CLLocationManagerDelegate {

    /// Whether the app may use the location right now, e.g. to pick the first screen.
    static var isGranted: Bool {
        switch CLLocationManager().authorizationStatus {
        case .authorizedAlways, .authorizedWhenInUse: true
        default: false
        }
    }

    private let manager = CLLocationManager()
    private var continuation: AsyncStream<CLAuthorizationStatus>.Continuation?

    /// The current status, then every change (the user's answer, a change made in Settings).
    /// Supports one collector at a time.
    var statuses: AsyncMapSequence<AsyncStream<CLAuthorizationStatus>, LocationPermissionStatus> {
        let authorizationStatuses = AsyncStream<CLAuthorizationStatus> { continuation in
            self.continuation = continuation
            continuation.yield(manager.authorizationStatus)
            manager.delegate = self
        }
        // `map` handles one status at a time, so a slow `.denied` check can't reorder them.
        return authorizationStatuses.map { await Self.permissionStatus(for: $0) }
    }

    /// Shows the system dialog; does nothing once the user has answered (iOS asks only once).
    func request() {
        manager.requestWhenInUseAuthorization()
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        continuation?.yield(manager.authorizationStatus)
    }

    private static func permissionStatus(for status: CLAuthorizationStatus) async -> LocationPermissionStatus {
        switch status {
        case .notDetermined:
            return .notDetermined
        case .restricted:
            return .restricted
        case .denied:
            // `.denied` also means Location Services are off for the whole device. Apple warns that
            // `locationServicesEnabled()` can block, so it runs off the main thread.
            let servicesEnabled = await Task.detached { CLLocationManager.locationServicesEnabled() }.value
            return servicesEnabled ? .permanentlyDenied : .servicesDisabled
        case .authorizedAlways, .authorizedWhenInUse:
            return .granted
        @unknown default:
            return .notDetermined
        }
    }
}
