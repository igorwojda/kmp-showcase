import SwiftUI
import KMPObservableViewModelSwiftUI
import SharedLogic

struct ContentView: View {
    @StateViewModel var homeViewModelStateFlow = HomeViewModelStateFlow()

    var body: some View {
        if let phrase = homeViewModelStateFlow.launchPhraseValue {
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
