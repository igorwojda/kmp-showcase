package com.igorwojda.showcase.data

import com.igorwojda.showcase.data.model.ForecastResponseModel
import com.igorwojda.showcase.domain.model.CurrentWeatherModel
import com.igorwojda.showcase.domain.model.DailyWeatherModel
import com.igorwojda.showcase.domain.model.ForecastModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/** Downloads weather data from the Open-Meteo API. */
class ForecastRepository(
    private val httpClient: HttpClient,
) {
    /**
     * Current weather plus a [forecastDays]-day daily forecast for the given coordinates.
     *
     * Throws on network / parsing failure.
     */
    suspend fun getForecast(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        forecastDays: Int = DEFAULT_FORECAST_DAYS,
    ): ForecastModel {
        val response: ForecastResponseModel = httpClient.get(BASE_URL) {
            // TODO: Use object to build query parameters instead of appending them manually.
            url.parameters.apply {
                append("latitude", latitude.toString())
                append("longitude", longitude.toString())
                append("current", "temperature_2m,wind_speed_10m,weather_code")
                append("daily", "temperature_2m_min,temperature_2m_max,weather_code")
                append("forecast_days", forecastDays.toString())
                append("timezone", "auto")
            }
        }.body()

        return response.toForecast()
    }

    private companion object {
        const val BASE_URL = "https://api.open-meteo.com/v1/forecast"
        const val DEFAULT_LATITUDE = 52.23 // Warsaw
        const val DEFAULT_LONGITUDE = 21.01
        const val DEFAULT_FORECAST_DAYS = 7
    }
}

// TODO: Nested Mappers?
private fun ForecastResponseModel.toForecast() = ForecastModel(
    latitude = latitude,
    longitude = longitude,
    current = CurrentWeatherModel(
        time = current.time,
        temperature = current.temperature,
        temperatureUnit = currentUnits.temperature,
        windSpeed = current.windSpeed,
        windSpeedUnit = currentUnits.windSpeed,
        weatherCode = current.weatherCode,
    ),
    daily = daily.time.indices.map { i ->
        DailyWeatherModel(
            date = daily.time[i],
            temperatureMin = daily.temperatureMin[i],
            temperatureMax = daily.temperatureMax[i],
            temperatureUnit = dailyUnits.temperatureMax,
            weatherCode = daily.weatherCode[i],
        )
    },
)
