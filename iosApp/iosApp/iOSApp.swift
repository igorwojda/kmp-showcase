import SwiftUI
import iosBridge

@main
struct iOSApp: App {

    init() {
        initializeKoin()
    }

    var body: some Scene {
        WindowGroup {
            ForecastScreen()
        }
    }
}
