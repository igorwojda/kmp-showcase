import SwiftUI

struct WeatherDetail: View {
    let label: String
    let value: String

    var body: some View {
        VStack {
            Text(label.uppercased())
                .font(.caption2)
            Text(value)
                .font(.subheadline.weight(.medium))
        }
    }
}
