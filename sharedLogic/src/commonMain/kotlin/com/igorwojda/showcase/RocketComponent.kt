package com.igorwojda.showcase

import LaunchListResponse
import RocketLaunch
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json

class RocketComponent {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    private suspend fun getDateOfLastSuccessfulLaunch(): String {
        val response: LaunchListResponse =
            httpClient.get("https://lldev.thespacedevs.com/2.3.0/launches/previous/?mode=list&limit=10&format=json").body()
        val lastSuccessLaunch = response.results.first { it.status.id == 3 }
        val date = Instant.parse(lastSuccessLaunch.launchDateUTC)
            .toLocalDateTime(TimeZone.currentSystemDefault())

        return "${date.month} ${date.day}, ${date.year}"
    }

    suspend fun launchPhrase(): String =
        try {
            "The last successful launch was on ${getDateOfLastSuccessfulLaunch()} 🚀"
        } catch (e: Exception) {
            println("Exception during getting the date of the last successful launch $e")
            "Error occurred"
        }
}
