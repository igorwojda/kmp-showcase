import SwiftUI
import KMPObservableViewModelSwiftUI
import iosBridge

struct WeeklyForecastScreen: View {
    @StateViewModel private var viewModel = provideWeeklyForecastViewModel()
    @State private var toast: String?

    var body: some View {
        NavigationStack {
            // SKIE turns `states` into an AsyncSequence, so SwiftUI can observe the store directly.
            Observing(viewModel.states) { state in
                // `onEnum(of:)` makes the sealed interface exhaustive – a new state stops compiling here.
                switch onEnum(of: state) {
                case .content(let content):
                    WeeklyForecastContent(forecast: content.forecast)
                case .error(let error):
                    ErrorContent(message: error.message, onRetry: reload)
                case .loading:
                    ProgressView()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .navigationTitle("Weather")
            .navigationDestination(for: LocalDate.self) { date in
                DailyForecastScreen(date: date)
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Reload", action: reload)
                }
            }
        }
        .overlay(alignment: .bottom) {
            if let toast {
                Toast(message: toast)
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
        viewModel.onIntent(intent: WeeklyForecastIntentReload.shared)
    }
}

private struct WeeklyForecastContent: View {
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
                    NavigationLink(value: day.date) {
                        DailyForecastRow(
                            day: day,
                            dayLabel: dayLabel(for: day.date, at: index),
                            scaleMin: scaleMin,
                            scaleMax: scaleMax
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(16)
        }
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

#Preview("Content") {
    WeeklyForecastContent(forecast: previewForecast)
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
        previewDay(month: 9, day: 22, min: 11.0, max: 19.0, weatherCode: 2),
        previewDay(month: 9, day: 23, min: 9.5, max: 17.0, weatherCode: 61),
        previewDay(month: 9, day: 24, min: 8.0, max: 15.5, weatherCode: 3),
        previewDay(month: 9, day: 25, min: 10.0, max: 21.0, weatherCode: 0),
        previewDay(month: 9, day: 26, min: 12.0, max: 23.5, weatherCode: 1),
    ]
)

private func previewDay(month: Int32, day: Int32, min: Double, max: Double, weatherCode: Int32) -> DailyWeatherModel {
    DailyWeatherModel(
        date: LocalDate(year: 2026, month: month, day: day),
        temperatureMin: min,
        temperatureMax: max,
        temperatureUnit: "°C",
        weatherCode: weatherCode,
        sunrise: LocalDateTime(year: 2026, month: month, day: day, hour: 6, minute: 32, second: 0, nanosecond: 0),
        sunset: LocalDateTime(year: 2026, month: month, day: day, hour: 18, minute: 41, second: 0, nanosecond: 0),
        precipitationSum: 0,
        precipitationUnit: "mm",
        precipitationProbabilityMax: KotlinInt(int: 10),
        windSpeedMax: 14,
        windSpeedUnit: "km/h",
        hourlyTemperatures: []
    )
}
