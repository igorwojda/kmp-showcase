plugins {
    alias(libs.plugins.kotlinMultiplatform)
    // SKIE belongs only in the module that builds the framework; it covers every exported module.
    alias(libs.plugins.skie)
}

// iOS composition root: bundles every feature into the single framework the iOS app links, and starts Koin.
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "iosBridge"
            isStatic = true
            // Kotlin/Native cannot infer a bundle ID for a static framework; set it explicitly to silence the warning.
            binaryOption("bundleId", "com.igorwojda.showcase.iosBridge")
            // Swift uses the features' ViewModels, StoreViewModel (their base class) and the LocalDate / LocalDateTime
            // of the domain models, so these types must be visible to Swift.
            export(project(":feature:base"))
            export(project(":feature:forecast"))
            export(project(":feature:permission"))
            export(libs.kotlinx.datetime)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Only api dependencies can be exported
            api(project(":feature:base"))
            api(project(":feature:forecast"))
            api(project(":feature:permission"))
            api(libs.kotlinx.datetime)
        }
    }
}

skie {
    features {
        // https://skie.touchlab.co/features/flows-in-swiftui
        enableSwiftUIObservingPreview.set(true)
    }
}
