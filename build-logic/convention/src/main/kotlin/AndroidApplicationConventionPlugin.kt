import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Android application with Jetpack Compose UI: SDK versions, JVM target, release build type and all app
 * dependencies (feature modules, Compose, Navigation 3). The app module declares only its identity
 * (namespace, application id, version).
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun lib(alias: String) = libs.findLibrary(alias).get()
        fun version(alias: String) = libs.findVersion(alias).get().requiredVersion.toInt()

        pluginManager.apply(libs.findPlugin("androidApplication").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("composeCompiler").get().get().pluginId)

        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        extensions.configure<ApplicationExtension> {
            compileSdk = version("android-compileSdk")

            defaultConfig {
                minSdk = version("android-minSdk")
                targetSdk = version("android-targetSdk")
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
                        "proguard-rules.pro"
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
            "implementation"(platform(lib("androidx-compose-bom")))
            "implementation"(lib("androidx-activity-compose"))
            "implementation"(lib("androidx-compose-runtime"))
            "implementation"(lib("androidx-compose-foundation"))
            "implementation"(lib("androidx-compose-material3"))
            "implementation"(lib("androidx-compose-ui"))
            "implementation"(lib("androidx-compose-uiToolingPreview"))
            "implementation"(lib("androidx-lifecycle-runtimeCompose"))
            "implementation"(lib("androidx-lifecycle-viewmodelCompose"))
            "implementation"(lib("androidx-lifecycle-viewmodelNavigation3"))
            "implementation"(lib("androidx-navigation3-runtime"))
            "implementation"(lib("androidx-navigation3-ui"))

            "debugImplementation"(lib("androidx-compose-uiTooling"))
        }
    }
}
