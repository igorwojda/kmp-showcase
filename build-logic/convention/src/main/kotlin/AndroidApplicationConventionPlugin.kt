import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Android application with Jetpack Compose UI: SDK versions, JVM target, release build type, Android Lint (see
 * [AndroidLintConventionPlugin]) and all app dependencies (feature modules, Compose, Navigation 3). The app module
 * declares only its identity (namespace, application id, version).
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(libs.pluginId("androidApplication"))
            pluginManager.apply(libs.pluginId("composeCompiler"))
            pluginManager.apply(libs.pluginId("showcase-android-lint"))

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = libs.version("android-compileSdk").toInt()

                defaultConfig {
                    minSdk = libs.version("android-minSdk").toInt()
                    targetSdk = libs.version("android-targetSdk").toInt()
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                "implementation"(project(":feature:forecast"))
                "implementation"(project(":feature:permission"))

                // Jetpack Compose UI
                "implementation"(platform(libs.lib("androidx-compose-bom")))
                "implementation"(libs.lib("androidx-activity-compose"))
                "implementation"(libs.lib("androidx-compose-runtime"))
                "implementation"(libs.lib("androidx-compose-foundation"))
                "implementation"(libs.lib("androidx-compose-material3"))
                "implementation"(libs.lib("androidx-compose-ui"))
                "implementation"(libs.lib("androidx-compose-uiToolingPreview"))
                "implementation"(libs.lib("androidx-lifecycle-runtimeCompose"))
                "implementation"(libs.lib("androidx-lifecycle-viewmodelCompose"))
                "implementation"(libs.lib("androidx-lifecycle-viewmodelNavigation3"))
                "implementation"(libs.lib("androidx-navigation3-runtime"))
                "implementation"(libs.lib("androidx-navigation3-ui"))

                "debugImplementation"(libs.lib("androidx-compose-uiTooling"))
            }
        }
}
