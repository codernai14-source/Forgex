plugins {
    id("forgex.android.library")
}

android {
    namespace = "com.forgex.mobile.core.common"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.android)
}
