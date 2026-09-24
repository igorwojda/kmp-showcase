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

            // Network. Kept as implementation so the Ktor types stay out of the Obj-C framework header
            // (:iosBridge exports this module); feature modules that call the API declare Ktor themselves.
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            // Type-safe requests: @Resource classes become URL path + query parameters
            implementation(libs.ktor.client.resources)
            implementation(libs.ktor.serialization.kotlinx.json)
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
