plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //Google Firebase plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.beyoureyes"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.beyoureyes"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.google.firebase:firebase-database-ktx:20.2.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Google Firebase
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-firestore:24.7.1")
    // 익명 계정을 위한 dependency 추가
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    // google login
    implementation ("com.google.android.gms:play-services-auth:19.2.0")
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth-ktx")

    //MPAndroidChart
    implementation("com.github.PhilJay:MpAndroidChart:v3.1.0")

    //openCV
    implementation(project(":opencv"))

    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-common:17.0.0")
    implementation ("com.google.android.material:material:1.6.0")
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-korean:16.0.0")


    implementation ("androidx.camera:camera-core:1.2.0-alpha01")
    implementation ("androidx.camera:camera-camera2:1.2.0-alpha01")
    implementation ("androidx.camera:camera-lifecycle:1.2.0-alpha01")
    implementation ("androidx.camera:camera-video:1.2.0-alpha01")
    implementation ("androidx.camera:camera-view:1.2.0-alpha01")
    implementation ("androidx.camera:camera-extensions:1.2.0-alpha01")


}