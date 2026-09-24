plugins {
    alias(libs.plugins.showcase.feature)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // :feature:base keeps Ktor as implementation, so every module calling the API declares it itself.
            implementation(libs.ktor.client.core)
            // Type-safe requests: @Resource classes become URL path + query parameters
            implementation(libs.ktor.client.resources)
        }
    }
}
