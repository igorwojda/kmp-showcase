import SwiftUI
import KMPObservableViewModelSwiftUI
import iosBridge

struct DailyForecastScreen: View {
    @StateViewModel private var viewModel: DailyForecastViewModel
    private let date: LocalDate

    init(date: LocalDate) {
        self.date = date
        _viewModel = StateViewModel(wrappedValue: provideDailyForecastViewModel(date: date))
    }

    var body: some View {
        Observing(viewModel.states) { state in
            switch onEnum(of: state) {
            case .content(let content):
                DailyForecastContent(day: content.day)
            case .error(let error):
                ErrorContent(message: error.message) {
                    viewModel.onIntent(intent: DailyForecastIntentRetry.shared)
                }
            case .loading:
                ProgressView()
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .navigationTitle(date.foundationDate.fullDateLabel)
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct DailyForecastContent: View {
    let day: DailyWeatherModel

    var body: some View {
        let condition = WeatherCondition.companion.fromCode(code: day.weatherCode)

        ScrollView {
            VStack(spacing: 12) {
                VStack(spacing: 4) {
                    Text(condition.symbol)
                        .font(.system(size: 64))
                    Text("\(Int(day.temperatureMax.rounded()))\(day.temperatureUnit)")
                        .font(.system(size: 44, weight: .bold))
                    Text("Low \(Int(day.temperatureMin.rounded()))\(day.temperatureUnit)")
                    Text(condition.label)
                        .font(.headline)
                }
                .frame(maxWidth: .infinity)
                .padding(20)
                .background(Color.accentColor.opacity(0.15), in: RoundedRectangle(cornerRadius: 12))

                if !day.hourlyTemperatures.isEmpty {
                    Text("Hourly")
                        .font(.headline)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, 8)
                    HourlyTemperatureRow(hours: day.hourlyTemperatures, temperatureUnit: day.temperatureUnit)
                }

                DetailRow(label: "Precipitation", value: "\(day.precipitationSum) \(day.precipitationUnit)")
                DetailRow(
                    label: "Chance of precipitation",
                    value: day.precipitationProbabilityMax.map { "\($0.intValue)%" } ?? "–"
                )
                DetailRow(label: "Max wind", value: "\(Int(day.windSpeedMax.rounded())) \(day.windSpeedUnit)")
                DetailRow(label: "Sunrise", value: day.sunrise.foundationDate.timeOfDayLabel)
                DetailRow(label: "Sunset", value: day.sunset.foundationDate.timeOfDayLabel)
            }
            .padding(16)
        }
    }
}

private struct HourlyTemperatureRow: View {
    let hours: [HourlyTemperatureModel]
    let temperatureUnit: String

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            LazyHStack(spacing: 8) {
                ForEach(Array(hours.enumerated()), id: \.offset) { _, hour in
                    VStack(spacing: 4) {
                        Text(hour.time.foundationDate.timeOfDayLabel)
                            .font(.caption)
                        Text("\(Int(hour.temperature.rounded()))\(temperatureUnit)")
                            .font(.subheadline.weight(.bold))
                    }
                    .frame(width: 64)
                    .padding(.vertical, 12)
                    .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 12))
                }
            }
        }
    }
}

private struct DetailRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(.subheadline)
            Spacer()
            Text(value)
                .font(.subheadline.weight(.bold))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 12))
    }
}

#Preview {
    DailyForecastContent(
        day: DailyWeatherModel(
            date: LocalDate(year: 2026, month: 9, day: 23),
            temperatureMin: 9.5,
            temperatureMax: 17.0,
            temperatureUnit: "°C",
            weatherCode: 61,
            sunrise: LocalDateTime(year: 2026, month: 9, day: 23, hour: 6, minute: 32, second: 0, nanosecond: 0),
            sunset: LocalDateTime(year: 2026, month: 9, day: 23, hour: 18, minute: 41, second: 0, nanosecond: 0),
            precipitationSum: 4.2,
            precipitationUnit: "mm",
            precipitationProbabilityMax: KotlinInt(int: 80),
            windSpeedMax: 22,
            windSpeedUnit: "km/h",
            hourlyTemperatures: (0..<24).map { hour in
                HourlyTemperatureModel(
                    time: LocalDateTime(year: 2026, month: 9, day: 23, hour: Int32(hour), minute: 0, second: 0, nanosecond: 0),
                    temperature: 9.5 + Double(hour % 12) * 0.6
                )
            }
        )
    )
}
