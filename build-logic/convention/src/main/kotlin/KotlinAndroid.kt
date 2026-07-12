import com.sungchanbong.unsplashcard.buildlogic.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

val JAVA_VERSION: JavaVersion = JavaVersion.VERSION_17

private fun VersionCatalog.intVersion(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()

val Project.compileSdkVersion: Int get() = libs.intVersion("compileSdk")
val Project.minSdkVersion: Int get() = libs.intVersion("minSdk")
val Project.targetSdkVersion: Int get() = libs.intVersion("targetSdk")

fun Project.configureKotlinAndroidJvm() {
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
}

fun Project.configureKotlinJvmOnly() {
    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
}
