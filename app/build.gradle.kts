plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.firebaseauth"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.firebaseauth"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidbrowserhelper)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("com.google.android.gms:play-services-auth:20.2.0")
    implementation ("androidx.activity:activity-ktx:1.3.1")
    implementation("androidx.credentials:credentials:1.2.0") // Versão mais recente da AndroidX Credentials API
    implementation("androidx.credentials:credentials-play-services-auth:1.2.0") // Integração com o Google Play Services
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1") // Google Identity Services
    implementation ("com.google.firebase:firebase-auth:21.0.1")
    implementation ("com.google.firebase:firebase-firestore:24.0.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation ("io.coil-kt:coil-compose:2.2.2") // Verifique se há uma versão mais recente
    implementation ("androidx.compose.material:material-icons-extended:1.4.0") // Versão do Compose Icons
    implementation ("com.google.accompanist:accompanist-permissions:0.26.3-alpha")
    implementation ("androidx.compose.foundation:foundation:1.3.0") // Adicionar dependência do Compose Permissions
    implementation ("androidx.compose.material:material:1.3.0")// Adicionar dependência de Material
    implementation ("com.google.accompanist:accompanist-permissions:0.28.0")

    // Certifique-se de usar a versão mais recente

}

