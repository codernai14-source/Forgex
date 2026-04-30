plugins {
    id("forgex.android.library")
}

android {
    namespace = "com.forgex.mobile.core.model"
}

dependencies {
    implementation(project(":core:common"))
}
