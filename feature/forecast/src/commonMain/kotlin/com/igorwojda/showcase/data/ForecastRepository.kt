package com.igorwojda.showcase.data

import com.igorwojda.showcase.data.model.ForecastRequestModel
import com.igorwojda.showcase.data.model.ForecastResponseModel
import com.igorwojda.showcase.domain.model.CurrentWeatherModel
import com.igorwojda.showcase.domain.model.DailyWeatherModel
import com.igorwojda.showcase.domain.model.ForecastModel
import com.igorwojda.showcase.domain.model.HourlyTemperatureModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.http.URLProtocol
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Downloads weather data from the Open-Meteo API.
 *
 * Responses are kept in an in-memory cache (per request) for [CACHE_TTL], so screens opened after
 * the first request (e.g. DailyForecastScreen) reuse the downloaded forecast instead of hitting the
 * network again.
 */
class ForecastRepository(
    private val httpClient: HttpClient,
) {
    private val cacheMutex = Mutex()
    private val cache = mutableMapOf<ForecastRequestModel, CachedForecast>()

    /**
     * Current weather plus a [forecastDays]-day daily forecast for the given coordinates.
     *
     * Returns the cached forecast when it is younger than [CACHE_TTL], unless [forceRefresh] is set.
     * Throws on network / parsing failure.
     */
    suspend fun getForecast(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        forecastDays: Int = DEFAULT_FORECAST_DAYS,
        forceRefresh: Boolean = false,
    ): ForecastModel {
        val request = ForecastRequestModel(latitude, longitude, forecastDays)

        // The lock also stops concurrent callers from downloading the same forecast twice.
        return cacheMutex.withLock {
            val now = Clock.System.now()
            cache[request]
                ?.takeUnless { forceRefresh || now - it.fetchedAt >= CACHE_TTL }
                ?.forecast
                ?: fetchForecast(request).also { cache[request] = CachedForecast(it, now) }
        }
    }

    /**
     * Weather for a single [date] of the (cached) default forecast, or `null` when the forecast
     * doesn't contain [date].
     *
     * Throws on network / parsing failure.
     */
    suspend fun getDailyWeather(date: LocalDate): DailyWeatherModel? =
        getForecast().daily.firstOrNull { it.date == date }

    private suspend fun fetchForecast(request: ForecastRequestModel): ForecastModel =
        httpClient.get(request) {
            url {
                protocol = URLProtocol.HTTPS
                host = HOST
            }
        }.body<ForecastResponseModel>().toForecast()

    private class CachedForecast(
        val forecast: ForecastModel,
        val fetchedAt: Instant,
    )

    private companion object {
        const val HOST = "api.open-meteo.com"
        const val DEFAULT_LATITUDE = 52.23 // Warsaw
        const val DEFAULT_LONGITUDE = 21.01
        const val DEFAULT_FORECAST_DAYS = 7
        val CACHE_TTL = 15.minutes
    }
}

// TODO: Nested Mappers?
private fun ForecastResponseModel.toForecast(): ForecastModel {
    val hourlyByDate = hourly.time.indices
        .map { i -> HourlyTemperatureModel(time = hourly.time[i], temperature = hourly.temperature[i]) }
        .groupBy { it.time.date }

    return ForecastModel(
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
                sunrise = daily.sunrise[i],
                sunset = daily.sunset[i],
                precipitationSum = daily.precipitationSum[i],
                precipitationUnit = dailyUnits.precipitationSum,
                precipitationProbabilityMax = daily.precipitationProbabilityMax[i],
                windSpeedMax = daily.windSpeedMax[i],
                windSpeedUnit = dailyUnits.windSpeedMax,
                hourlyTemperatures = hourlyByDate[daily.time[i]].orEmpty(),
            )
        },
    )
}
