plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.GalleryYou.Pixel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.GalleryYou.Pixel"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "1.0.0"
    }

    // Clave fija incluida en el repo: así cada versión nueva se instala encima de la anterior
    // sin tener que desinstalar (los ajustes se conservan).
    signingConfigs {
        create("galeria") {
            storeFile = file("galeria.keystore")
            storePassword = "galeria123"
            keyAlias = "galeria"
            keyPassword = "galeria123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("galeria")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")

    // Reproductor de video
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    // Miniaturas de fotos y videos
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")
}
