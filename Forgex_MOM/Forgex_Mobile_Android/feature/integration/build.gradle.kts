plugins {
    id("forgex.android.library.compose")
}

android {
    namespace = "com.forgex.mobile.feature.integration"
}

dependencies {
    implementation(project(":core:component"))
    implementation(project(":core:navigation"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
}

