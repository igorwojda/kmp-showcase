plugins {
    alias(libs.plugins.showcase.android.application)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.igorwojda.showcase"

    defaultConfig {
        applicationId = "com.igorwojda.showcase"
        versionCode = 1
        versionName = "1.0"
    }
}
