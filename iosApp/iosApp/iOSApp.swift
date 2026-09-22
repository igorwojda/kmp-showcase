import SwiftUI
import SharedLogic

@main
struct iOSApp: App {

    init() {
        KoinIosKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ForecastScreen()
        }
    }
}