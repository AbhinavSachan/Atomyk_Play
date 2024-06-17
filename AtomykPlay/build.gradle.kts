buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics.gradle)
    }
}
//plugins {
//    alias(libs.plugins.devtools.ksp) apply false
//}
allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
tasks.register("clean",Delete::class){
    delete(rootProject.layout.buildDirectory)
}