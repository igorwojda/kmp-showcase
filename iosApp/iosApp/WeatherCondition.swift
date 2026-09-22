/// WMO weather interpretation code (as returned by Open-Meteo) rendered as a label and a symbol.
///
/// Mirrors the Android `WeatherCondition` enum. See https://open-meteo.com/en/docs for the full code table.
enum WeatherCondition {
    case clearSky
    case mainlyClear
    case partlyCloudy
    case overcast
    case fog
    case drizzle
    case freezingDrizzle
    case rain
    case freezingRain
    case snow
    case snowGrains
    case rainShowers
    case snowShowers
    case thunderstorm
    case thunderstormWithHail
    case unknown

    init(code: Int32) {
        switch code {
        case 0: self = .clearSky
        case 1: self = .mainlyClear
        case 2: self = .partlyCloudy
        case 3: self = .overcast
        case 45, 48: self = .fog
        case 51, 53, 55: self = .drizzle
        case 56, 57: self = .freezingDrizzle
        case 61, 63, 65: self = .rain
        case 66, 67: self = .freezingRain
        case 71, 73, 75: self = .snow
        case 77: self = .snowGrains
        case 80, 81, 82: self = .rainShowers
        case 85, 86: self = .snowShowers
        case 95: self = .thunderstorm
        case 96, 99: self = .thunderstormWithHail
        default: self = .unknown
        }
    }

    var label: String {
        switch self {
        case .clearSky: "Clear sky"
        case .mainlyClear: "Mainly clear"
        case .partlyCloudy: "Partly cloudy"
        case .overcast: "Overcast"
        case .fog: "Fog"
        case .drizzle: "Drizzle"
        case .freezingDrizzle: "Freezing drizzle"
        case .rain: "Rain"
        case .freezingRain: "Freezing rain"
        case .snow: "Snow"
        case .snowGrains: "Snow grains"
        case .rainShowers: "Rain showers"
        case .snowShowers: "Snow showers"
        case .thunderstorm: "Thunderstorm"
        case .thunderstormWithHail: "Thunderstorm with hail"
        case .unknown: "Unknown"
        }
    }

    var symbol: String {
        switch self {
        case .clearSky: "☀️"
        case .mainlyClear: "🌤️"
        case .partlyCloudy: "⛅"
        case .overcast: "☁️"
        case .fog: "🌫️"
        case .drizzle: "🌦️"
        case .freezingDrizzle: "🌧️"
        case .rain: "🌧️"
        case .freezingRain: "🌧️"
        case .snow: "❄️"
        case .snowGrains: "🌨️"
        case .rainShowers: "🌦️"
        case .snowShowers: "🌨️"
        case .thunderstorm: "⛈️"
        case .thunderstormWithHail: "⛈️"
        case .unknown: "❔"
        }
    }
}
