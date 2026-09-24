import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Kotlin Multiplatform library targeting Android and iOS.
 *
 * The Android namespace is derived from the module path, e.g. `:feature:base` → `com.igorwojda.showcase.feature.base`.
 */
class BaseFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(libs.pluginId("kotlinMultiplatform"))
            pluginManager.apply(libs.pluginId("androidMultiplatformLibrary"))

            extensions.configure<KotlinMultiplatformExtension> {
                iosArm64()
                iosSimulatorArm64()

                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                    namespace = "com.igorwojda.showcase" + path.replace(':', '.')
                    compileSdk = libs.version("android-compileSdk").toInt()
                    minSdk = libs.version("android-minSdk").toInt()

                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_11)
                    }
                }
            }
        }
}
