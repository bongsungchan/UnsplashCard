import java.util.Properties

plugins {
    id("sungchanbong.android.library")
    id("sungchanbong.android.hilt")
}

val unsplashAccessKey: Provider<String> = providers
    .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText
    .map { text ->
        Properties().apply { text.reader().use { load(it) } }
            .getProperty("UNSPLASH_ACCESS_KEY")
    }
    .orElse(providers.gradleProperty("UNSPLASH_ACCESS_KEY"))
    .orElse("")

android {
    namespace = "com.sungchanbong.data"
    defaultConfig {
        val key = unsplashAccessKey.get()
        buildConfigField("String", "UNSPLASH_ACCESS_KEY", "\"$key\"")
        buildConfigField("String", "UNSPLASH_BASE_URL", "\"https://api.unsplash.com/\"")
    }
    buildFeatures { buildConfig = true }
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.paging.runtime)

    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

}
