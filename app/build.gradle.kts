plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.nfbeats"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nfbeats"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["redirectHostName"] = "www.spotify.com"
        manifestPlaceholders["redirectSchemeName"] = "https"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(files("C:\\Users\\citrus\\Downloads\\NFBeats_v0.2\\NFBeats\\app\\libs\\spotify-app-remote-release-0.8.0.aar"))
    implementation(files("C:\\Users\\citrus\\Downloads\\NFBeats_v0.2\\NFBeats\\app\\libs\\spotify-auth-release-2.1.0.aar"))
    implementation(files("C:\\Users\\citrus\\Downloads\\NFBeats_v0.2\\NFBeats\\app\\libs\\gson-2.10.1.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}