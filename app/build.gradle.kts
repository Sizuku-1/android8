plugins {
    id("com.android.application")
}

android {
    namespace = "cn.edu.sicnu.weather"
    compileSdk = 37
    defaultConfig {
        applicationId = "cn.edu.sicnu.weather"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.13.2")
}
