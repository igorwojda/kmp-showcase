import SwiftUI
import KMPObservableViewModelSwiftUI
import forecast

/// Cold-to-warm gradient used by the daily temperature range bars.
private let temperatureGradient = LinearGradient(
    colors: [
        Color(red: 0x4F / 255, green: 0xC3 / 255, blue: 0xF7 / 255),
        Color(red: 0xFF / 255, green: 0xB7 / 255, blue: 0x4D / 255),
    ],
    startPoint: .leading,
    endPoint: .trailing
)

struct ForecastScreen: View {
    @StateViewModel private var viewModel = provideForecastViewModel()
    @State private var toast: String?

    var body: some View {
        NavigationStack {
            // SKIE turns `states` into an AsyncSequence, so SwiftUI can observe the store directly.
            Observing(viewModel.states) { state in
                // `onEnum(of:)` makes the sealed interface exhaustive – a new state stops compiling here.
                switch onEnum(of: state) {
                case .content(let content):
                    ForecastContentView(forecast: content.forecast)
                case .error(let error):
                    ErrorView(message: error.message, onRetry: reload)
                case .loading:
                    ProgressView()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .navigationTitle("Weather")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Reload", action: reload)
                }
            }
        }
        .overlay(alignment: .bottom) {
            if let toast {
                ToastView(message: toast)
            }
        }
        .animation(.easeInOut, value: toast)
        // Auto-dismiss; a new toast restarts the timer because the task is keyed on the message.
        .task(id: toast) {
            guard toast != nil else { return }
            try? await Task.sleep(for: .seconds(2))
            toast = nil
        }
        // `.task` runs while the screen is visible, so the store sees the subscriber come and go.
        .task {
            for await action in viewModel.actions {
                switch onEnum(of: action) {
                case .showToast(let showToast):
                    toast = showToast.message
                }
            }
        }
    }

    private func reload() {
        viewModel.sendIntent(intent: ForecastIntentReload.shared)
    }
}

private struct ForecastContentView: View {
    let forecast: ForecastModel

    var body: some View {
        // A shared scale keeps the range bars of all days comparable.
        let scaleMin = forecast.daily.map(\.temperatureMin).min() ?? 0
        let scaleMax = forecast.daily.map(\.temperatureMax).max() ?? 0

        ScrollView {
            LazyVStack(spacing: 12) {
                CurrentWeatherCard(
                    current: forecast.current,
                    latitude: forecast.latitude,
                    longitude: forecast.longitude
                )

                Text("\(forecast.daily.count)-day forecast")
                    .font(.headline)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.top, 8)

                ForEach(Array(forecast.daily.enumerated()), id: \.offset) { index, day in
                    DailyForecastRow(
                        day: day,
                        dayLabel: dayLabel(for: day.date, at: index),
                        scaleMin: scaleMin,
                        scaleMax: scaleMax
                    )
                }
            }
            .padding(16)
        }
    }
}

private struct CurrentWeatherCard: View {
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

private struct WeatherDetail: View {
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

private struct DailyForecastRow: View {
    let day: DailyWeatherModel
    let dayLabel: String
    let scaleMin: Double
    let scaleMax: Double

    var body: some View {
        let condition = WeatherCondition.companion.fromCode(code: day.weatherCode)

        HStack(spacing: 12) {
            VStack(alignment: .leading) {
                Text(dayLabel)
                    .font(.subheadline.weight(.semibold))
                Text(day.date.foundationDate.dayOfMonthLabel)
                    .font(.caption)
            }
            .frame(width: 72, alignment: .leading)

            Text(condition.symbol)
                .font(.system(size: 24))

            Text("\(Int(day.temperatureMin.rounded()))°")
                .font(.subheadline)
                .frame(width: 36, alignment: .trailing)

            TemperatureRangeBar(
                low: day.temperatureMin,
                high: day.temperatureMax,
                scaleMin: scaleMin,
                scaleMax: scaleMax
            )

            Text("\(Int(day.temperatureMax.rounded()))\(day.temperatureUnit)")
                .font(.subheadline.weight(.bold))
                .frame(width: 52, alignment: .leading)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 12))
    }
}

/// Horizontal bar showing where `low`...`high` sits inside the `scaleMin`...`scaleMax` range
/// shared by every day of the forecast.
private struct TemperatureRangeBar: View {
    let low: Double
    let high: Double
    let scaleMin: Double
    let scaleMax: Double

    var body: some View {
        let scaleSpan = scaleMax - scaleMin > 0 ? scaleMax - scaleMin : 1
        let startFraction = clamp((low - scaleMin) / scaleSpan, 0, 1)
        let endFraction = clamp((high - scaleMin) / scaleSpan, startFraction, 1)
        // A single-degree day would otherwise collapse to an invisible bar.
        let fillFraction = max(endFraction - startFraction, 0.05)
        let leadingFraction = min(startFraction, 1 - fillFraction)

        GeometryReader { geometry in
            Capsule()
                .fill(temperatureGradient)
                .frame(width: geometry.size.width * fillFraction)
                .offset(x: geometry.size.width * leadingFraction)
        }
        .frame(height: 8)
        .background(Color(.systemFill), in: Capsule())
    }

    private func clamp(_ value: Double, _ lower: Double, _ upper: Double) -> Double {
        min(max(value, lower), upper)
    }
}

private struct ErrorView: View {
    let message: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            Text("⚠️")
                .font(.system(size: 40))
            Text(message)
                .foregroundStyle(.red)
                .multilineTextAlignment(.center)
            Button("Reload", action: onRetry)
                .buttonStyle(.borderedProminent)
        }
        .padding(24)
    }
}

/// SwiftUI has no toast; a bottom capsule that the screen dismisses after a short delay stands in for it.
private struct ToastView: View {
    let message: String

    var body: some View {
        Text(message)
            .font(.subheadline)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(.regularMaterial, in: Capsule())
            .padding(.bottom, 24)
            .transition(.move(edge: .bottom).combined(with: .opacity))
    }
}

/// "Today" / "Tomorrow" for the first two days of the forecast, a weekday name for the rest.
private func dayLabel(for date: LocalDate, at index: Int) -> String {
    switch index {
    case 0: "Today"
    case 1: "Tomorrow"
    default: date.foundationDate.formatted(.dateTime.weekday(.abbreviated))
    }
}

private func coordinatesLabel(latitude: Double, longitude: Double) -> String {
    let latitudeHemisphere = latitude >= 0 ? "N" : "S"
    let longitudeHemisphere = longitude >= 0 ? "E" : "W"
    return String(format: "%.2f°%@ %.2f°%@", abs(latitude), latitudeHemisphere, abs(longitude), longitudeHemisphere)
}

private extension Date {
    /// "22 Sep" (order follows the device locale).
    var dayOfMonthLabel: String { formatted(.dateTime.day().month(.abbreviated)) }
    /// "14:30", always 24-hour like the Android screen.
    var timeOfDayLabel: String { formatted(.dateTime.hour(.twoDigits(amPM: .omitted)).minute(.twoDigits)) }
}

private extension LocalDate {
    /// The date at midnight in the device time zone, so `Calendar`-based formatting keeps the same calendar day.
    var foundationDate: Date {
        Calendar.current.date(from: DateComponents(
            year: Int(year),
            month: Int(month.ordinal) + 1,
            day: Int(day)
        )) ?? .distantPast
    }
}

private extension LocalDateTime {
    var foundationDate: Date {
        Calendar.current.date(from: DateComponents(
            year: Int(year),
            month: Int(month.ordinal) + 1,
            day: Int(day),
            hour: Int(hour),
            minute: Int(minute)
        )) ?? .distantPast
    }
}

#Preview("Content") {
    ForecastContentView(forecast: previewForecast)
}

#Preview("Error") {
    ErrorView(message: "Unable to reach the weather service", onRetry: {})
}

private let previewForecast = ForecastModel(
    latitude: 52.23,
    longitude: 21.01,
    current: CurrentWeatherModel(
        time: LocalDateTime(year: 2026, month: 9, day: 22, hour: 14, minute: 30, second: 0, nanosecond: 0),
        temperature: 18.4,
        temperatureUnit: "°C",
        windSpeed: 11.2,
        windSpeedUnit: "km/h",
        weatherCode: 2
    ),
    daily: [
        DailyWeatherModel(date: LocalDate(year: 2026, month: 9, day: 22), temperatureMin: 11.0, temperatureMax: 19.0, temperatureUnit: "°C", weatherCode: 2),
        DailyWeatherModel(date: LocalDate(year: 2026, month: 9, day: 23), temperatureMin: 9.5, temperatureMax: 17.0, temperatureUnit: "°C", weatherCode: 61),
        DailyWeatherModel(date: LocalDate(year: 2026, month: 9, day: 24), temperatureMin: 8.0, temperatureMax: 15.5, temperatureUnit: "°C", weatherCode: 3),
        DailyWeatherModel(date: LocalDate(year: 2026, month: 9, day: 25), temperatureMin: 10.0, temperatureMax: 21.0, temperatureUnit: "°C", weatherCode: 0),
        DailyWeatherModel(date: LocalDate(year: 2026, month: 9, day: 26), temperatureMin: 12.0, temperatureMax: 23.5, temperatureUnit: "°C", weatherCode: 1),
    ]
)
