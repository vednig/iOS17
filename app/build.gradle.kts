@file:Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")

import foundation.e.blisslauncher.buildsrc.Versions
import java.util.*

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.refine)
}

// Manifest version information
val versionMajor = 1
val versionMinor = 7
val versionPatch = 0

val localProps = Properties()
val localPropsFile = project.rootProject.file("local.properties")

if (localPropsFile.exists()) {
    localProps.load(localPropsFile.inputStream())
}

val appendDebugSuffix = (localProps.getProperty("appendDebugSuffix") ?: "true").toBoolean()
val keyStorePath = localProps.getProperty("keyStorePath") ?: "/keystore/debug.keystore"
val keyStorePassword = localProps.getProperty("keyStorePassword") ?: "android"
val signingKeyAlias = localProps.getProperty("keyAlias") ?: "androiddebugkey"
val signingKeyPassword = localProps.getProperty("keyPassword") ?: "android"

android {
    namespace = "foundation.e.blisslauncher"
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        applicationId = "foundation.e.blisslauncher"
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        versionCode = versionMajor * 100_00_00 + versionMinor * 10_00 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        renderscriptTargetApi = 28
        renderscriptSupportModeEnabled = true
        setProperty("archivesBaseName", "BlissLauncher-$versionName")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }

        debug {
            if (appendDebugSuffix) {
                applicationIdSuffix = ".debug"
            }
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(rootDir.path + keyStorePath)
            storePassword = keyStorePassword
            keyAlias = signingKeyAlias
            keyPassword = signingKeyPassword
        }
    }

    flavorDimensions.add("api")
    productFlavors {
        create("apiQ") {
            dimension = "api"
            minSdk = 29
            targetSdk = 29
        }

        create("apiR") {
            dimension = "api"
            minSdk = 29
            targetSdk = 29
        }

        create("apiS") {
            dimension = "api"
            minSdk = 29
            targetSdk = 29
        }
    }

    // Always show the result of every unit test, even if it passes.
    testOptions.unitTests.all { test ->
        test.testLogging { events("passed", "skipped", "failed", "standardOut", "standardError") }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions { jvmTarget = "1.8" }

    buildFeatures { viewBinding = true }

    lint { abortOnError = false }
}

dependencies {
    "apiQImplementation"(files("libs/lineage-sdk-q.jar"))
    "apiRImplementation"(files("libs/lineage-sdk-r.jar"))
    "apiSImplementation"(files("libs/e-ui-sdk-s.jar"))

    // Support Libs
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.localbroadcast)

    // Rx Java and Android
    implementation(libs.rx.android)
    implementation(libs.rx.binding)
    implementation(libs.rx.java)
    implementation(libs.rx.relay)

    // Room
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.retrofit.rxjava.adapter)

    // Okhttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Misc.
    implementation(libs.hoko.blur)
    implementation(libs.apache.commons)
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.greenrobot.eventbus)
    implementation(libs.circleindicator)
    implementation(libs.restriction.bypass)
    debugImplementation(libs.debug.db)
    coreLibraryDesugaring(libs.tools.desugar)

    // Testing dependencies
    testImplementation(libs.bundles.testing.unit)
    testImplementation(libs.bundles.testing.android)

    // elib
    implementation(libs.elib)
}
