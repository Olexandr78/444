plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.olexandr78.fourfourfour"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.olexandr78.fourfourfour"
        minSdk = 23
        // Android 14+ pre-grants SCHEDULE_EXACT_ALARM to apps targeting API 32 or lower.
        // This sideloaded app uses exact alarms for its explicitly enabled 03:00 scenario.
        targetSdk = 32
        versionCode = 2
        versionName = "1.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
