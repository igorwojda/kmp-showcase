import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/** `gradle/libs.versions.toml`. The generated type-safe accessors aren't available in build-logic sources. */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> = findLibrary(alias).get()

internal fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

/** Java version for `compileOptions`, from the catalog `java` entry. */
internal val VersionCatalog.javaVersion: JavaVersion
    get() = JavaVersion.toVersion(version("java"))

/** Kotlin `jvmTarget`, from the catalog `java` entry, so Java and Kotlin always emit the same bytecode version. */
internal val VersionCatalog.jvmTarget: JvmTarget
    get() = JvmTarget.fromTarget(version("java"))

internal fun VersionCatalog.pluginId(alias: String): String = findPlugin(alias).get().get().pluginId
