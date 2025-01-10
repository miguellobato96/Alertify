plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.alertify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.alertify"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // AndroidX libraries
    implementation(libs.appcompat)
    implementation(libs.material) // Material Components
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Google Play Services and Maps
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")


    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Networking library for API calls
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}
