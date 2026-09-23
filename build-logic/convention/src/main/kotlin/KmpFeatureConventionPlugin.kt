import co.touchlab.skie.plugin.configuration.SkieExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Feature module: a [KmpBaseFeatureConventionPlugin] library that depends on `:feature:base`
 * and holds its Android Jetpack Compose UI in `androidMain`. SKIE makes the iOS framework Swift-friendly.
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun lib(alias: String) = libs.findLibrary(alias).get()

        pluginManager.apply(KmpBaseFeatureConventionPlugin::class.java)
        pluginManager.apply(libs.findPlugin("composeCompiler").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("skie").get().get().pluginId)

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.commonMain.dependencies {
                api(project(":feature:base"))
            }
            sourceSets.androidMain.dependencies {
                // Jetpack Compose UI (Android screens)
                implementation(project.dependencies.platform(lib("androidx-compose-bom")))
                implementation(lib("androidx-compose-runtime"))
                implementation(lib("androidx-compose-foundation"))
                implementation(lib("androidx-compose-material3"))
                implementation(lib("androidx-compose-ui"))
                implementation(lib("androidx-compose-uiToolingPreview"))
                implementation(project.dependencies.platform(lib("koin-bom")))
                implementation(lib("koin-androidx-compose"))
            }
        }

        extensions.configure<SkieExtension> {
            // SwiftUI screens in src/iosMain/swift are compiled by the Xcode app target (which has the Swift
            // packages they import), so SKIE must not bundle them into the framework.
            swiftBundling {
                enabled.set(false)
            }
            features {
                // https://skie.touchlab.co/features/flows-in-swiftui
                enableSwiftUIObservingPreview.set(true)
            }
        }

        dependencies {
            // Renders @Preview in the IDE; the KMP android library plugin has no debug-only configuration.
            "androidRuntimeClasspath"(lib("androidx-compose-uiTooling"))
        }
    }
}
