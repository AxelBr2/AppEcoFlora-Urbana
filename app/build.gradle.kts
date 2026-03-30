plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose) // <--- Aplícalo aquí
    //PARA LA BASE DE DATOS
    id("com.google.gms.google-services")
}

android {
    namespace = "io.devexpert.appfloracdmx"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.devexpert.appfloracdmx"
        minSdk = 24
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
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
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    //PARA LA BASE DE DATOS
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    //Para imagenes
    implementation ("io.coil-kt:coil-compose:2.4.0") // Verifica si hay una versión más reciente
    //Para la localizacion
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    //FLOW ROW
    implementation ("com.google.accompanist:accompanist-flowlayout:0.31.0-alpha")
    implementation ("com.google.firebase:firebase-storage-ktx") // Firebase Storage
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation ("com.google.firebase:firebase-firestore") // Firestore (opcional)
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")
    implementation ("androidx.activity:activity-compose:1.7.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("io.github.jan-tennert.supabase:storage-kt:1.3.2")
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.31.3-beta")

    implementation ("com.google.api-client:google-api-client-android:1.33.2")
    implementation ("com.google.api-client:google-api-client-gson:1.33.2")
    implementation ("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.17.0")
    implementation ("androidx.compose.material:material-icons-extended")
}