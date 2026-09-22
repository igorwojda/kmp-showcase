import SwiftUI
import SharedLogic

@main
struct iOSApp: App {

    init() {
        // TODO: Where this ficntion is defined?
        // SKIE exposes top-level Kotlin functions as top-level Swift functions.
        doInitKoin(config: nil)
    }

    var body: some Scene {
        WindowGroup {
            ForecastScreen()
        }
    }
}
