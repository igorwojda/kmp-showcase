import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * ktlint (run by Spotless) over every Kotlin source and Gradle script in the repository. Applied to the root project
 * only. Rules are configured in `.editorconfig`.
 *
 * - `./gradlew spotlessCheck` - fails on formatting violations
 * - `./gradlew spotlessApply` - fixes them in place
 */
class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply(libs.pluginId("spotless"))

            extensions.configure<SpotlessExtension> {
                kotlin {
                    // Excluded in the tree itself (not targetExclude) so build folders, e.g. Xcode's, are never walked
                    target(
                        fileTree(rootDir) {
                            include("**/*.kt", "**/*.kts")
                            exclude("**/build/**", "**/.gradle/**", "**/.kotlin/**")
                        },
                    )

                    // Jetpack Compose rules on top of the standard ktlint rules
                    val composeRules = libs.lib("composeRules-ktlint").get()
                    ktlint(libs.version("ktlint"))
                        .customRuleSets(listOf("${composeRules.module}:${composeRules.versionConstraint.requiredVersion}"))

                    endWithNewline()
                }

                // Keep formatting out of the `check` task, so it runs as a separate check (CI job)
                isEnforceCheck = false
            }
        }
}
