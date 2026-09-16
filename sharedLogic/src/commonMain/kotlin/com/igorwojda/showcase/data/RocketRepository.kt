package com.igorwojda.showcase.data

import LaunchListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class RocketRepository(
    private val httpClient: HttpClient = createHttpClient(),
) {
    private suspend fun getDateOfLastSuccessfulLaunch(): String {
        val response: LaunchListResponse =
            httpClient.get("https://lldev.thespacedevs.com/2.3.0/launches/previous/?mode=list&limit=10&format=json").body()
        val lastSuccessLaunch = response.results.first { it.status.id == 3 }
        val date = Instant.parse(lastSuccessLaunch.launchDateUTC)
            .toLocalDateTime(TimeZone.currentSystemDefault())

        return "${date.month} ${date.day}, ${date.year}"
    }

    /** Throws on network / parsing failure. */
    suspend fun launchPhrase(): String =
        "The last successful launch was on ${getDateOfLastSuccessfulLaunch()} 🚀"
}
