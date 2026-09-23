plugins {
    `kotlin-dsl`
}

dependencies {
    // compileOnly: the root build applies these plugins (apply false), so they're already on the classpath.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.skie.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.showcase.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("kmpBaseFeature") {
            id = libs.plugins.showcase.kmp.basefeature.get().pluginId
            implementationClass = "KmpBaseFeatureConventionPlugin"
        }
        register("kmpFeature") {
            id = libs.plugins.showcase.kmp.feature.get().pluginId
            implementationClass = "KmpFeatureConventionPlugin"
        }
    }
}
