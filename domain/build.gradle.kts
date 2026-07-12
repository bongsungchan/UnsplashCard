plugins {

    id("sungchanbong.jvm.library")
}

dependencies {
    api(libs.paging.common)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
