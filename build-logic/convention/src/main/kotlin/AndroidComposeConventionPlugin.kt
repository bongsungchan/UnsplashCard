import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.sungchanbong.unsplashcard.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.findByType(LibraryExtension::class.java)?.apply {
            buildFeatures.compose = true
        }
        extensions.findByType(ApplicationExtension::class.java)?.apply {
            buildFeatures.compose = true
        }

        extensions.configure<ComposeCompilerGradlePluginExtension> {
            includeSourceInformation.set(false)
        }

        dependencies {
            val bom = platform(libs.findLibrary("androidx-compose-bom").get())
            "implementation"(bom)
            "implementation"(libs.findLibrary("androidx-ui").get())
            "implementation"(libs.findLibrary("androidx-ui-graphics").get())
            "implementation"(libs.findLibrary("androidx-ui-tooling-preview").get())
            "implementation"(libs.findLibrary("androidx-material3").get())
            "implementation"(libs.findLibrary("androidx-material-icons-extended").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
        }
    }
}
