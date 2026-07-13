plugins {
    id("forgex.android.library")
    id("forgex.android.hilt")
}

android {
    namespace = "com.forgex.mobile.core.sync"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.okhttp.core)
    implementation(libs.retrofit.core)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.okhttp.core)
}
