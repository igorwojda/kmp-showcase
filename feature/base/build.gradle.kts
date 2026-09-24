plugins {
    alias(libs.plugins.showcase.basefeature)
}

kotlin {
    // Every feature module depends on this module, so shared libraries are exposed as api.
    sourceSets {
        commonMain.dependencies {
            // Presentation (MVI)
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.kmp.observableviewmodel.core)
            api(libs.flowmvi.compose)
            // Remote debugger (IDE plugin / desktop app). The KMP android library plugin has no
            // debug/release source sets, so it ships in release builds too.
            api(libs.flowmvi.debugger)
            api(libs.kotlinx.coroutines)
            api(libs.kotlinx.datetime)

            // DI
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.koin.core.viewmodel)

            // Network
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            // Type-safe requests: @Resource classes become URL path + query parameters
            api(libs.ktor.client.resources)
            api(libs.ktor.serialization.kotlinx.json)
        }
        androidMain.dependencies {
            // Provides the Android engine for Ktor
            implementation(libs.ktor.client.android)
            // androidContext() / androidLogger(), used by the app module when starting Koin
            api(libs.koin.android)
        }
        iosMain.dependencies {
            // Provides the Darwin engine for Ktor
            implementation(libs.ktor.client.darwin)
        }
    }
}
