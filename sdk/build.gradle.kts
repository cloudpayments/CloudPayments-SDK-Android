import org.jetbrains.kotlin.config.KotlinCompilerVersion
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

mavenPublishing {
    signAllPublications()
    publishToMavenCentral(SonatypeHost.S01)
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("javadoc"))
}

android {
    namespace = "ru.cloudpayments.sdk"
    compileSdk = 33

    defaultConfig {
        minSdk = 19
        multiDexEnabled = false
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        debug {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Deps.coroutinesVer}")

    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.fragment:fragment-ktx:1.5.7")
    implementation("androidx.arch.core:core-runtime:2.2.0")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    implementation("com.squareup.retrofit2:retrofit:${Deps.retrofitVer}")
    implementation("com.squareup.retrofit2:converter-gson:${Deps.retrofitVer}")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:${Deps.okHttpVer}"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    implementation("ru.tinkoff.decoro:decoro:${Deps.decoroVer}")

    implementation("org.jsoup:jsoup:1.16.1")

    //Multidex support
    implementation("androidx.multidex:multidex:2.0.1")

    // dagger2
    implementation("com.google.dagger:dagger:${Deps.daggerVer}")
    implementation("com.google.dagger:dagger-android:${Deps.daggerVer}")
    implementation("com.google.dagger:dagger-android-support:${Deps.daggerVer}")
    kapt("com.google.dagger:dagger-compiler:${Deps.daggerVer}")
    kapt("com.google.dagger:dagger-android-processor:${Deps.daggerVer}")
}