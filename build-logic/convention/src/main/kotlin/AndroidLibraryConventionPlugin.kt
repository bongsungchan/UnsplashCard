import com.android.build.api.dsl.LibraryExtension
import com.sungchanbong.unsplashcard.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        extensions.configure<LibraryExtension> {
            compileSdk = compileSdkVersion
            defaultConfig.minSdk = minSdkVersion

            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
        }
        configureKotlinAndroidJvm()
        dependencies {
            "testImplementation"(libs.findBundle("unit-test").get())
        }
    }
}
