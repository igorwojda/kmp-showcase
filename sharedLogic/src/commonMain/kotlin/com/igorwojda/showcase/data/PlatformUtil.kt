package com.igorwojda.showcase.data
//TODO: detete file
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
