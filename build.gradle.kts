plugins {
    // Code quality checks run once from the root project over the whole repository
    alias(libs.plugins.showcase.spotless)
    alias(libs.plugins.showcase.detekt)

    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.skie) apply false
}
