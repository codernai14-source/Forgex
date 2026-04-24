plugins {
    id("forgex.android.library")
}

android {
    namespace = "com.forgex.mobile.core.architecture"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
