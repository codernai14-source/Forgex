plugins {
    id("forgex.android.library")
}

android {
    namespace = "com.forgex.mobile.core.testing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.android)
}
