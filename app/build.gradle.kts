plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.effektor.timeliner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.effektor.timeliner"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // BuildConfig field for the web service endpoint
        buildConfigField("String", "TIMELINE_UPLOAD_ENDPOINT", "\"https://example.com/api/timeline\"")
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
    
    buildFeatures {
        buildConfig = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    
    // DocumentFile for Storage Access Framework
    implementation("androidx.documentfile:documentfile:1.0.1")
    
    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // OkHttp for HTTP requests
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // WorkManager for background uploads (optional feature)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
