// Top-level build file where you can add configuration options common to all sub-projects/modules.
@file:Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.refine) apply false
    alias(libs.plugins.benmanes.versions) apply false
    id("foundation.e.blisslauncher.spotless")
}

val clean by tasks.existing(Delete::class) { delete(rootProject.buildDir) }
