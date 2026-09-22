import SwiftUI
import KMPObservableViewModelSwiftUI
import SharedLogic

struct ContentView: View {
    @StateViewModel var forecastViewModelStateFlow = ForecastViewModelStateFlow()

    var body: some View {
        if let phrase = forecastViewModelStateFlow.launchPhraseValue {
            Text(phrase)
        } else {
            ProgressView()
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
