plugins {
    id("forgex.android.library")
    id("forgex.android.hilt")
}

android {
    namespace = "com.forgex.mobile.core.datastore"
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
}
