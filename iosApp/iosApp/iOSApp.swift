import SwiftUI
import forecast

@main
struct iOSApp: App {

    init() {
        initializeKoin(config: nil)
    }

    var body: some Scene {
        WindowGroup {
            ForecastScreen()
        }
    }
}
