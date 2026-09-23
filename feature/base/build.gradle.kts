import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
       namespace = "com.igorwojda.showcase.feature.base"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
    }

    sourceSets {
        all {
            // Opt-in to experimental APIs used by KMP-ObservableViewModel library.
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        commonMain.dependencies {
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.kmp.observableviewmodel.core)
            api(libs.flowmvi.compose)
        }
    }
}
