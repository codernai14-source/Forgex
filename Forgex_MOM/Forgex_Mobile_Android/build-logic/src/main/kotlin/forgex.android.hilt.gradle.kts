import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

plugins {
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

extensions.configure<KaptExtension> {
    correctErrorTypes = true
}

dependencies {
    add("implementation", "com.google.dagger:hilt-android:2.52")
    add("kapt", "com.google.dagger:hilt-android-compiler:2.52")
}
