import SwiftUI
import KMPObservableViewModelSwiftUI
import SharedLogic

struct ContentView: View {
    @StateViewModel var viewModelStateFlow = ViewModelStateFlow()

    var body: some View {
        ListView(phrases: viewModelStateFlow.greetings)
    }
}

struct ListView: View {
    let phrases: Array<String>

    var body: some View {
        List(phrases, id: \.self) {
            Text($0)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
