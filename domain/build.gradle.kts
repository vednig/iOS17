@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

import foundation.e.blisslauncher.buildsrc.Versions

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "foundation.e.blisslauncher.domain"
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    kapt(libs.dagger.compiler)

    implementation(libs.rx.kotlin)
    implementation(libs.timber)
    testImplementation(libs.junit)
}
