import org.jetbrains.kotlin.config.KotlinCompilerVersion
import Common.getGitShortName
import Common.getGitCurrentBranch

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "ru.cloudpayments.demo"
    compileSdk = 33

    defaultConfig {
        applicationId = "ru.cloudpayments.demo"
        minSdk = 19
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    applicationVariants.all { variant ->
        variant.outputs.all { output ->
            var sh = ""
            if (!variant.buildType.isDebuggable) {
                sh = getGitShortName()
            }
            val branch = getGitCurrentBranch()
//            outputFileName = "${variant.name}-${variant.versionName}(${defaultConfig.versionCode})${sh}_[${branch}].apk"
            true
        }
    }
}

dependencies {
    implementation(project(":sdk"))
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))

    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.fragment:fragment-ktx:1.5.7")
    implementation("androidx.arch.core:core-runtime:2.2.0")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // material dialogs
    implementation("com.afollestad.material-dialogs:core:0.9.6.0")

    implementation("com.squareup.retrofit2:retrofit:${Deps.retrofitVer}")
    implementation("com.squareup.retrofit2:adapter-rxjava2:${Deps.retrofitVer}")
    implementation("com.squareup.retrofit2:converter-gson:${Deps.retrofitVer}")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:${Deps.okHttpVer}"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    implementation("ru.tinkoff.decoro:decoro:${Deps.decoroVer}")

    implementation("com.google.android.gms:play-services-wallet:19.1.0")

    //Multidex support
    implementation("androidx.multidex:multidex:2.0.1")

    // dagger2
    implementation("com.google.dagger:dagger:${Deps.daggerVer}")
    implementation("com.google.dagger:dagger-android:${Deps.daggerVer}")
    implementation("com.google.dagger:dagger-android-support:${Deps.daggerVer}")
    kapt("com.google.dagger:dagger-compiler:${Deps.daggerVer}")
    kapt("com.google.dagger:dagger-android-processor:${Deps.daggerVer}")

    // image loading and caching
    implementation("com.github.bumptech.glide:glide:4.15.1")

    //card io scanner
    implementation("io.card:android-sdk:5.5.1")
}