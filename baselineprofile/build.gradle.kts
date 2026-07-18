plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.bccard.unsplashexplorer.baselineprofile"

    // com.android.test 플러그인이라 bccard.android.library 컨벤션을 그대로 쓸 수 없다.
    // 다만 SDK 값은 버전 카탈로그에서 읽어, 다른 모듈과 어긋나지 않게 한다.
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()
    defaultConfig {
        minSdk = 28 // Baseline Profile 수집은 API 28+ 필요
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    // 구식 kotlinOptions 대신 compilerOptions — 다른 모듈(convention 플러그인)과 같은 방식.
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

baselineProfile {
    // 연결된 기기/에뮬레이터에서 수집한다.
    // 주의 — 생성된 프로파일을 `app/src/<variant>/generated/baselineProfiles/` 에
    // **커밋해야** 앱에 실제로 들어간다. 모듈만 만들어 두고 프로파일을 커밋하지 않으면
    // 스타트업 개선은 0이다. (이전 상태가 정확히 그랬다.)
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro)
}
