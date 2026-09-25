plugins {
    alias(libs.plugins.showcase.iosbridge)
}

// iOS composition root: bundles every feature into the single framework the iOS app links, and starts Koin.
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Features exported to Swift (their ViewModels and StoreViewModel, their base class)
            api(projects.feature.base)
            api(projects.feature.forecast)
        }
    }
}
