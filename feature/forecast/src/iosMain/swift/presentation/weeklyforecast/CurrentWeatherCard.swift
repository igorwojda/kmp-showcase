import SwiftUI
import iosBridge

struct CurrentWeatherCard: View {
    let current: CurrentWeatherModel
    let latitude: Double
    let longitude: Double

    var body: some View {
        let condition = WeatherCondition.companion.fromCode(code: current.weatherCode)

        VStack(spacing: 4) {
            Text(condition.symbol)
                .font(.system(size: 64))

            Text("\(Int(current.temperature.rounded()))\(current.temperatureUnit)")
                .font(.system(size: 44, weight: .bold))

            Text(condition.label)
                .font(.headline)

            HStack {
                Spacer()
                WeatherDetail(label: "Wind", value: "\(Int(current.windSpeed.rounded())) \(current.windSpeedUnit)")
                Spacer()
                WeatherDetail(label: "Updated", value: current.time.foundationDate.timeOfDayLabel)
                Spacer()
                WeatherDetail(label: "Location", value: coordinatesLabel(latitude: latitude, longitude: longitude))
                Spacer()
            }
            .padding(.top, 16)
        }
        .frame(maxWidth: .infinity)
        .padding(20)
        .background(Color.accentColor.opacity(0.15), in: RoundedRectangle(cornerRadius: 12))
    }
}

private func coordinatesLabel(latitude: Double, longitude: Double) -> String {
    let latitudeHemisphere = latitude >= 0 ? "N" : "S"
    let longitudeHemisphere = longitude >= 0 ? "E" : "W"
    return String(format: "%.2f°%@ %.2f°%@", abs(latitude), latitudeHemisphere, abs(longitude), longitudeHemisphere)
}
