import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

val fcmKey: File = rootProject.file("local.properties")
val fcmProperty = Properties()
fcmProperty.load(FileInputStream(fcmKey))

android {
    namespace = "project.social.whisper"
    compileSdk = 34

    defaultConfig {
        applicationId = "project.social.whisper"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField ("String", "FCM_KEY", fcmProperty.getProperty("SERVER_KEY"))
        buildConfigField ("String", "GEMINI", fcmProperty.getProperty("GEMINI_API"))
        buildConfigField ("String", "MAP_API", fcmProperty.getProperty("MAP_API"))
        buildConfigField ("String", "APP_SIGN", fcmProperty.getProperty("APP_SIGN"))
        buildConfigField ("String", "APP_ID", fcmProperty.getProperty("APP_ID"))
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        buildConfig = true
        viewBinding = true
    }
}


dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    //Lottie animation
    implementation("com.airbnb.android:lottie:6.3.0")

    //For Gemini API
    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")

    //Notification
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //For responsiveness
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    implementation("com.intuit.sdp:sdp-android:1.1.0")

    //Firebase
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage:20.3.0")

    //Google sign in/up
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //Image Picker
    implementation("com.github.dhaval2404:imagepicker:2.1")

    //Circular image view
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //View Drag
    implementation("com.github.hyuwah:DraggableView:1.0.1")

    //Floating hint design
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity:1.8.0")

    //Photo View to enable zoom in image
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.google.firebase:firebase-messaging:23.4.1")

    //For location
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    //ZegoCloud, Calling
    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")



}