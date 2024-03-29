plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
}

android {
    namespace = "com.github.ace4896.genshinstickers"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.github.ace4896.genshinstickers"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Setup content provider authority in manifest and BuildConfig
        val contentProviderAuthority = "$applicationId.stickercontentprovider"
        manifestPlaceholders["contentProviderAuthority"] = contentProviderAuthority
        buildConfigField("String", "CONTENT_PROVIDER_AUTHORITY", "\"$contentProviderAuthority\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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

    packaging {
        jniLibs {
            excludes += arrayOf("lib/*/libnative-imagetranscoder.so", "lib/*/libnative-filters.so")
        }

        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Compression of webp files during build causes problems with FileDescriptor in ContentProvider
    androidResources {
        noCompress += arrayOf("webp")
    }
}

dependencies {
    implementation(fileTree("libs").matching { include("*.jar") })

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.google.android.material)

    implementation(libs.fresco)
    implementation(libs.fresco.webpsupport)
    implementation(libs.fresco.animated.webp)
    implementation(libs.fresco.animated.base)
}
