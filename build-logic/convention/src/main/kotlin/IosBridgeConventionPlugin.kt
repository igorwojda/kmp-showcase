import co.touchlab.skie.plugin.configuration.SkieExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * iOS composition root: builds the single static `iosBridge` framework the iOS app links, with SKIE. The module
 * declares only the features it bundles, as `commonMain` `api` dependencies; each of them is exported to Swift.
 */
class IosBridgeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(libs.pluginId("kotlinMultiplatform"))
            // SKIE belongs only in the module that builds the framework; it covers every exported module.
            pluginManager.apply(libs.pluginId("skie"))

            extensions.configure<KotlinMultiplatformExtension> {
                listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
                    target.binaries.framework {
                        baseName = "iosBridge"
                        isStatic = true
                        // Kotlin/Native cannot infer a bundle ID for a static framework; set it explicitly to silence
                        // the warning.
                        binaryOption("bundleId", "com.igorwojda.showcase.iosBridge")
                        // Export every commonMain api dependency (only api dependencies can be exported), so a new
                        // feature is added to the framework with a single api(...) line in the module.
                        configurations.named(exportConfigurationName) {
                            extendsFrom(configurations.getByName("commonMainApi"))
                        }
                    }
                }

                sourceSets.commonMain.dependencies {
                    // Swift uses the LocalDate / LocalDateTime of the domain models, so these types must be visible
                    api(libs.lib("kotlinx-datetime"))
                }
            }

            extensions.configure<SkieExtension> {
                features {
                    // https://skie.touchlab.co/features/flows-in-swiftui
                    enableSwiftUIObservingPreview.set(true)
                }

                analytics {
                    // Skip the network call to Touchlab that otherwise runs on every iOS build.
                    disableUpload.set(true)
                }
            }
        }
}
