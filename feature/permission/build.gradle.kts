plugins {
    alias(libs.plugins.showcase.feature)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            // rememberLauncherForActivityResult (permission request), LocalActivity (rationale flag)
            implementation(libs.androidx.activity.compose)
            // LifecycleResumeEffect: re-checks the permission when the user returns from Settings
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
    }
}
