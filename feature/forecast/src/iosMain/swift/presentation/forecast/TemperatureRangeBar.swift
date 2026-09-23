import SwiftUI

/// Cold-to-warm gradient used by the daily temperature range bars.
private let temperatureGradient = LinearGradient(
    colors: [
        Color(red: 0x4F / 255, green: 0xC3 / 255, blue: 0xF7 / 255),
        Color(red: 0xFF / 255, green: 0xB7 / 255, blue: 0x4D / 255),
    ],
    startPoint: .leading,
    endPoint: .trailing
)

/// Horizontal bar showing where `low`...`high` sits inside the `scaleMin`...`scaleMax` range
/// shared by every day of the forecast.
struct TemperatureRangeBar: View {
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
