@file:Suppress("UnstableApiUsage", "JcenterRepositoryObsolete")

pluginManagement {
    repositories {
        includeBuild("build-logic")
        gradlePluginPortal()
        google()
        maven("https://maven.fabric.io/public")
        mavenCentral()
        jcenter()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven("https://maven.fabric.io/public")
        mavenCentral()
        jcenter()
        maven("https://jitpack.io")
    }
}

enableFeaturePreview("VERSION_CATALOGS")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

rootProject.name = "blisslauncher"
