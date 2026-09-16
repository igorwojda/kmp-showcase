package com.igorwojda.showcase.data

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
