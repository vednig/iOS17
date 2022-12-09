import foundation.e.blisslauncher.buildsrc.Versions

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "foundation.e.blisslauncher.benchmark"
    compileSdk = Versions.COMPILE_SDK

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions { jvmTarget = "1.8" }

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions.add("api")
    productFlavors {
        create("apiQ") { dimension = "api" }

        create("apiR") { dimension = "api" }

        create("apiS") { dimension = "api" }
    }

    buildTypes {
        // This benchmark buildType is used for benchmarking, and should function like your
        // release build (for example, with minification on). It"s signed with a debug key
        // for easy local/CI testing.
        val benchmark by creating {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.test.espresso)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.junit)
}

androidComponents { beforeVariants { it.enable = it.buildType == "benchmark" } }
