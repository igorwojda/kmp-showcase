package com.igorwojda.showcase.data

import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GreetingRepository(
    private val rocketRepository: RocketRepository = RocketRepository(),
) {
    private val platform: Platform = getPlatform()

    /**
     * Emits greetings one by one. Network failures from [RocketRepository] propagate
     * as flow exceptions so callers decide how to surface them (e.g. FlowMvi `recover`).
     */
    fun greetFlow(): Flow<String> = flow {
        emit(if (Random.nextBoolean()) "Hi!" else "Hello!")
        delay(1.seconds)
        emit("Guess what this is! > ${platform.name.reversed()}")
        delay(1.seconds)
        emit(daysPhrase())
        emit(rocketRepository.launchPhrase())
    }
}
