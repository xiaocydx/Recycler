import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.xiaocydx"
            afterEvaluate { from(components["release"]) }
        }
    }
}

android {
    namespace = "com.xiaocydx.cxrv.paging"
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFile("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        unitTests { isIncludeAndroidResources = true }
    }
    configurations {
        testImplementation.extendsFrom(compileOnly)
    }
}

dependencies {
    compileOnly(project(":cxrv"))
    compileOnly("androidx.appcompat:appcompat:1.2.0")
    compileOnly("androidx.recyclerview:recyclerview:1.2.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    testImplementation("junit:junit:4.+")
    testImplementation("com.google.truth:truth:1.0")
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}