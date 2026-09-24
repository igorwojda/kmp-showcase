import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

/**
 * Android Lint for an Android application module. Rules are configured in `lint.xml`, on top of the Lint defaults.
 * Applied by [AndroidApplicationConventionPlugin], so the app module doesn't configure Lint.
 *
 * The tasks are aliases for AGP's `lint` / `lintFix`, named after the other linters:
 *
 * - `./gradlew lintCheck` - fails on Lint violations (warnings included)
 * - `./gradlew lintApply` - also applies the fixes Lint considers safe
 */
class AndroidLintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<ApplicationExtension> {
                lint {
                    warningsAsErrors = true
                    lintConfig = rootProject.file("lint.xml")
                    // Covers Android library dependencies. KMP library modules (:feature:*) are not covered: AGP's
                    // Kotlin Multiplatform library plugin does not create lint tasks for them yet.
                    checkDependencies = true
                }
            }

            tasks.register("lintCheck") {
                group = "verification"
                description = "Checks that sourcecode satisfies Android Lint rules."
                dependsOn("lint")
            }

            tasks.register("lintApply") {
                group = "verification"
                description = "Applies safe Android Lint suggestions to sourcecode in-place."
                dependsOn("lintFix")
            }
        }
    }
}
