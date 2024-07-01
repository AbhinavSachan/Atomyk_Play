import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
//    id("com.google.devtools.ksp")
}

val properties = Properties()
properties.load(project.rootProject.file("local.properties").reader())

android {
    signingConfigs {
        create("release") {
            storeFile = rootProject.file(properties.getProperty("file_name"))
            storePassword = properties.getProperty("password")
            keyAlias = properties.getProperty("alias")
            keyPassword = properties.getProperty("password")
        }
    }
    namespace = "com.atomykcoder.atomykplay"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.atomykcoder.atomykplay"
        minSdk = 24
        targetSdk = 34
        versionCode = 28
        versionName = "1.8.19"
        multiDexEnabled = true

        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        //to enable coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4") this library below at line 126
        //we have to enable this option if we don't do this there will be a red carpet (effective warnings) in AppUtils from line 419 to last line
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    packaging {
        resources {
            excludes.apply {
                add("META-INF/INDEX.LIST")
                add("META-INF/io.netty.versions.properties")
            }
        }
    }
    bundle {
        language {
            enableSplit = false
        }
    }
    lint {
        checkReleaseBuilds = true
        // Or, if you prefer, you can continue to check for errors in release builds,
        // but continue the build even when errors are found:
        abortOnError = false
    }
}

dependencies {
    //this here is for memory leaks
//    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'

    //event bus implementations
    implementation(libs.eventbus)

    // https://mvnrepository.com/artifact/org/jaudiotagger
    implementation(libs.jaudiotagger)

    //legacy implementations
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.asynclayoutinflater)
    implementation(libs.constraintlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.media)
    implementation(libs.swiperefreshlayout)
    implementation(libs.kotlinx.coroutines.android)
    //navigation bars
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    implementation(libs.glide)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.glide.compiler)

    //permission
    implementation(libs.dexter)

    implementation(libs.recyclerview)
    implementation(libs.palette.ktx)
    implementation(libs.circleimageview)
    implementation(libs.firebase.crashlytics)
    implementation(libs.core.splashscreen)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.lottie)
    // Retrofit
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.retrofit) {
        exclude(module = "okhttp")
    }

    implementation(libs.jsoup)
    implementation(libs.converter.scalars)

    implementation(libs.fastscroll)

}
