plugins {
    id("sungchanbong.android.library")
    id("sungchanbong.android.compose")
    id("sungchanbong.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sungchanbong.feature"

}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    implementation(libs.coil.compose)
}
