plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    jacoco
}

android {
    namespace = "eu.micer.distro"
    compileSdk = 36

    defaultConfig {
        applicationId = "eu.micer.distro"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(17)
}

// JaCoCo configuration
buildscript {
    dependencies {
        classpath("org.jacoco:org.jacoco.core:0.8.11")
    }
}

// JaCoCo task for unit test coverage report
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/databinding/**/*.class",
        "**/android/databinding/*Binding.class",
        "**/android/databinding/*",
        "**/androidx/databinding/*",
        "**/BR.*",
        "**/eu/micer/distro/DistroApp.*",
        "**/eu/micer/distro/MainActivity.*"
    )

    val debugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug").get()) {
        exclude(fileFilter)
    }
    val javaDebugTree = fileTree(layout.buildDirectory.dir("intermediates/javac/debug").get()) {
        exclude(fileFilter)
    }

    sourceDirectories.setFrom(
        files(
            "src/main/java",
            "src/main/kotlin"
        )
    )
    classDirectories.setFrom(files(debugTree, javaDebugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("jacoco/testDebugUnitTest.exec")
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    })
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // OkHttp for downloads (direct, bypassing Ktor to avoid buffering delays)
    implementation(libs.okhttp)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Timber (Logging)
    implementation(libs.timber)
    
    // Kotlin Serialization (JSON parsing)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.icons.font.awesome.solid.android)
    implementation(libs.composables.icons.font.awesome.regular.android)
    implementation(libs.composables.icons.font.awesome.brands.android)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.arch.core.testing)
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}
