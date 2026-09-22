package com.igorwojda.showcase.ui.forecast

/**
 * WMO weather interpretation code (as returned by Open-Meteo) rendered as a label and a symbol.
 *
 * See https://open-meteo.com/en/docs for the full code table.
 */
enum class WeatherCondition(val label: String, val symbol: String) {
    ClearSky("Clear sky", "☀️"),
    MainlyClear("Mainly clear", "🌤️"),
    PartlyCloudy("Partly cloudy", "⛅"),
    Overcast("Overcast", "☁️"),
    Fog("Fog", "🌫️"),
    Drizzle("Drizzle", "🌦️"),
    FreezingDrizzle("Freezing drizzle", "🌧️"),
    Rain("Rain", "🌧️"),
    FreezingRain("Freezing rain", "🌧️"),
    Snow("Snow", "❄️"),
    SnowGrains("Snow grains", "🌨️"),
    RainShowers("Rain showers", "🌦️"),
    SnowShowers("Snow showers", "🌨️"),
    Thunderstorm("Thunderstorm", "⛈️"),
    ThunderstormWithHail("Thunderstorm with hail", "⛈️"),
    Unknown("Unknown", "❔"),
    ;

    companion object {
        fun fromCode(code: Int): WeatherCondition = when (code) {
            0 -> ClearSky
            1 -> MainlyClear
            2 -> PartlyCloudy
            3 -> Overcast
            45, 48 -> Fog
            51, 53, 55 -> Drizzle
            56, 57 -> FreezingDrizzle
            61, 63, 65 -> Rain
            66, 67 -> FreezingRain
            71, 73, 75 -> Snow
            77 -> SnowGrains
            80, 81, 82 -> RainShowers
            85, 86 -> SnowShowers
            95 -> Thunderstorm
            96, 99 -> ThunderstormWithHail
            else -> Unknown
        }
    }
}
