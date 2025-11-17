plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // alias(libs.plugins.kotlin.compose) // Removed - migrated to XML layouts
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mytheclipse.quizbattle"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mytheclipse.quizbattle"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Optional: Disable debugging features in release
            isDebuggable = false
            
            // Optional: Version name suffix
            versionNameSuffix = ""
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = false // Disabled - migrated to XML layouts
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    // Activity & AppCompat for XML layouts
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    
    // Compose - Removed (migrated to XML layouts)
    // implementation(platform(libs.androidx.compose.bom))
    // implementation(libs.androidx.compose.ui)
    // implementation(libs.androidx.compose.ui.graphics)
    // implementation(libs.androidx.compose.ui.tooling.preview)
    // implementation(libs.androidx.compose.material3)
    // implementation(libs.androidx.compose.material.icons)
    // implementation(libs.androidx.activity.compose)
    
    // Lifecycle - Removed Compose variants
    // implementation(libs.androidx.lifecycle.viewmodel.compose)
    // implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Navigation - Removed Compose navigation
    // implementation(libs.androidx.navigation.compose)
    
    // Coil - Removed Compose integration
    // implementation(libs.coil.compose)
    
    // Material Components for XML layouts
    implementation(libs.material)
    
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // androidTestImplementation(platform(libs.androidx.compose.bom)) // Removed
    // debugImplementation(libs.androidx.compose.ui.tooling) // Removed
}