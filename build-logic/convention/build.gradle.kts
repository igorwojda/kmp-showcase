plugins {
    `kotlin-dsl`
}

dependencies {
    // compileOnly: the root build applies these plugins (apply false), so they're already on the classpath.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.skie.gradlePlugin)
    // implementation: applied only through the convention plugins below, so build-logic has to provide them.
    implementation(libs.spotless.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id =
                libs.plugins.showcase.android.application
                    .get()
                    .pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLint") {
            id =
                libs.plugins.showcase.android.lint
                    .get()
                    .pluginId
            implementationClass = "AndroidLintConventionPlugin"
        }
        register("baseFeature") {
            id =
                libs.plugins.showcase.basefeature
                    .get()
                    .pluginId
            implementationClass = "BaseFeatureConventionPlugin"
        }
        register("feature") {
            id =
                libs.plugins.showcase.feature
                    .get()
                    .pluginId
            implementationClass = "FeatureConventionPlugin"
        }
        register("iosBridge") {
            id =
                libs.plugins.showcase.iosbridge
                    .get()
                    .pluginId
            implementationClass = "IosBridgeConventionPlugin"
        }
        register("spotless") {
            id =
                libs.plugins.showcase.spotless
                    .get()
                    .pluginId
            implementationClass = "SpotlessConventionPlugin"
        }
        register("detekt") {
            id =
                libs.plugins.showcase.detekt
                    .get()
                    .pluginId
            implementationClass = "DetektConventionPlugin"
        }
    }
}
