plugins {
    id("sungchanbong.android.application")
    id("sungchanbong.android.compose")
    id("sungchanbong.android.hilt")
}

android {
    namespace = "com.sungchanbong.unsplashcard"

    defaultConfig {
        applicationId = "com.sungchanbong.unsplashcard"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":core"))
    implementation(project(":feature"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.coil.compose)
    kspAndroidTest(libs.hilt.compiler)
}