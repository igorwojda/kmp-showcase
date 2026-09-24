import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Detekt static analysis over every Kotlin source and Gradle script in the repository, without configuring each
 * module. Applied to the root project only. Rules are configured in `detekt.yml`, on top of the Detekt defaults.
 *
 * - `./gradlew detektCheck` - fails on rule violations
 * - `./gradlew detektApply` - also fixes auto-correctable violations in place
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(libs.pluginId("detekt"))

            val detektCheck =
                tasks.register<Detekt>("detektCheck") {
                    description = "Checks that sourcecode satisfies detekt rules."
                    autoCorrect = false
                }

            val detektApply =
                tasks.register<Detekt>("detektApply") {
                    description = "Applies detekt auto-corrections to sourcecode in-place."
                    autoCorrect = true
                }

            listOf(detektCheck, detektApply).forEach { task ->
                task.configure {
                    group = "verification"
                    parallel = true
                    setSource(rootDir)
                    include("**/*.kt", "**/*.kts")
                    exclude("**/build/**", "**/.gradle/**", "**/.kotlin/**", "**/generated/**", "**/resources/**")

                    config.setFrom(rootDir.resolve("detekt.yml"))
                    // New rules from Detekt upgrades work without touching detekt.yml
                    buildUponDefaultConfig = true

                    reports {
                        html.required.set(true)
                        xml.required.set(true)
                    }
                }
            }
        }
}
