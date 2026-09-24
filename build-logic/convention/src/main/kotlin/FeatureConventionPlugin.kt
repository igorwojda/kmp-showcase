import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Feature module: a [BaseFeatureConventionPlugin] library that depends on `:feature:base`
 * and holds its Android Jetpack Compose UI in `androidMain`. The iOS framework is built by `:iosBridge`.
 */
class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(BaseFeatureConventionPlugin::class.java)
            pluginManager.apply(libs.pluginId("composeCompiler"))

            extensions.configure<KotlinMultiplatformExtension> {
                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                    // Android host (JVM) unit tests
                    withHostTest {}
                }

                sourceSets.commonMain.dependencies {
                    api(project(":feature:base"))
                }
                // Shared by all test source sets (androidHostTest, iosTest)
                sourceSets.commonTest.dependencies {
                    implementation(libs.lib("kotlin-test"))
                }
                sourceSets.androidMain.dependencies {
                    // Jetpack Compose UI (Android screens)
                    implementation(project.dependencies.platform(libs.lib("androidx-compose-bom")))
                    implementation(libs.lib("androidx-compose-runtime"))
                    implementation(libs.lib("androidx-compose-foundation"))
                    implementation(libs.lib("androidx-compose-material3"))
                    implementation(libs.lib("androidx-compose-ui"))
                    implementation(libs.lib("androidx-compose-uiToolingPreview"))
                    implementation(project.dependencies.platform(libs.lib("koin-bom")))
                    implementation(libs.lib("koin-androidx-compose"))
                }
            }

            dependencies {
                // Renders @Preview in the IDE; the KMP android library plugin has no debug-only configuration.
                "androidRuntimeClasspath"(libs.lib("androidx-compose-uiTooling"))
            }
        }
}
