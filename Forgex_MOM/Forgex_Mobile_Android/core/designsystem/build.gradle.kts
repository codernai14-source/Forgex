plugins {
    id("forgex.android.library.compose")
}

android {
    namespace = "com.forgex.mobile.core.designsystem"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
}
