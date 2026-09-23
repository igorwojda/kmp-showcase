import SwiftUI
import forecast

struct DailyForecastRow: View {
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
