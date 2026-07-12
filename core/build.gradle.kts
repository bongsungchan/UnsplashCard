plugins {
    id("sungchanbong.android.library")
    id("sungchanbong.android.compose")
}

android {
    namespace = "com.sungchanbong.core"
}

dependencies {
    api(project(":domain"))
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.coil.compose)
}
