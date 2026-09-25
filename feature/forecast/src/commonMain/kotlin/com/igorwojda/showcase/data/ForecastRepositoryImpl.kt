package com.igorwojda.showcase.data

import com.igorwojda.showcase.data.model.ForecastRequestModel
import com.igorwojda.showcase.data.model.ForecastResponseModel
import com.igorwojda.showcase.domain.model.CurrentWeatherModel
import com.igorwojda.showcase.domain.model.DailyWeatherModel
import com.igorwojda.showcase.domain.model.ForecastModel
import com.igorwojda.showcase.domain.model.HourlyTemperatureModel
import com.igorwojda.showcase.domain.repository.ForecastRepository
import com.igorwojda.showcase.domain.repository.LocationRepository
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
 * Downloads weather data for the device location ([LocationRepository]) from the Open-Meteo API.
 *
 * The latest forecast is kept in an in-memory cache for [CACHE_TTL], so screens opened after the first request
 * (e.g. DailyForecastScreen) reuse the downloaded forecast instead of reading the location and hitting the
 * network again. The location is read only when a forecast is downloaded.
 */
internal class ForecastRepositoryImpl(
    private val httpClient: HttpClient,
    private val locationRepository: LocationRepository,
) : ForecastRepository {
    private val cacheMutex = Mutex()
    private var cache: CachedForecast? = null

    /** A [FORECAST_DAYS]-day forecast; the cached one while it is younger than [CACHE_TTL]. */
    override suspend fun getForecast(forceRefresh: Boolean): ForecastModel =
        // The lock also stops concurrent callers from downloading the same forecast twice.
        cacheMutex.withLock {
            val now = Clock.System.now()
            cache
                ?.takeUnless { forceRefresh || now - it.fetchedAt >= CACHE_TTL }
                ?.forecast
                ?: fetchForecast().also { cache = CachedForecast(it, now) }
        }

    override suspend fun getDailyWeather(date: LocalDate): DailyWeatherModel? = getForecast().daily.firstOrNull { it.date == date }

    private suspend fun fetchForecast(): ForecastModel {
        val location = locationRepository.getCurrentLocation()
        val request = ForecastRequestModel(location.latitude, location.longitude, FORECAST_DAYS)

        return httpClient
            .get(request) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = HOST
                }
            }.body<ForecastResponseModel>()
            .toForecast()
    }

    private data class CachedForecast(
        val forecast: ForecastModel,
        val fetchedAt: Instant,
    )

    private companion object {
        const val HOST = "api.open-meteo.com"
        const val FORECAST_DAYS = 7
        val CACHE_TTL = 15.minutes
    }
}

private fun ForecastResponseModel.toForecast(): ForecastModel {
    val hourlyByDate =
        hourly.time.indices
            .map { i -> HourlyTemperatureModel(time = hourly.time[i], temperature = hourly.temperature[i]) }
            .groupBy { it.time.date }

    return ForecastModel(
        latitude = latitude,
        longitude = longitude,
        current =
            CurrentWeatherModel(
                time = current.time,
                temperature = current.temperature,
                temperatureUnit = currentUnits.temperature,
                windSpeed = current.windSpeed,
                windSpeedUnit = currentUnits.windSpeed,
                weatherCode = current.weatherCode,
            ),
        daily =
            daily.time.indices.map { i ->
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
