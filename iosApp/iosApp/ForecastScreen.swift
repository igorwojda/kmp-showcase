import SwiftUI
import KMPObservableViewModelSwiftUI
import SharedLogic

struct ForecastScreen: View {
    @StateViewModel var forecastViewModelStateFlow = ForecastViewModelStateFlow()

    var body: some View {
        if let phrase = forecastViewModelStateFlow.launchPhraseValue {
            Text(phrase)
        } else {
            ProgressView()
        }
    }
}

#Preview {
    ForecastScreen()
}
