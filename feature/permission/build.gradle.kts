plugins {
    alias(libs.plugins.showcase.kmp.feature)
}

kotlin {
    android {
        // Unit tests for the Android permission status logic
        withHostTest {}
    }

    sourceSets {
        androidMain.dependencies {
            // rememberLauncherForActivityResult (permission request), LocalActivity (rationale flag)
            implementation(libs.androidx.activity.compose)
            // LifecycleResumeEffect: re-checks the permission when the user returns from Settings
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        // Created by withHostTest above, so there's no type-safe accessor for it.
        getByName("androidHostTest").dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
