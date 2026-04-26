plugins {
    id("forgex.android.library")
    id("forgex.android.hilt")
}

android {
    namespace = "com.forgex.mobile.core.device"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
