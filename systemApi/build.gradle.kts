plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

apply(from = "../gradle/spotless.gradle")

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 23
        targetSdk = 33

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("beta") { initWith(getByName("release")) }
    }

    buildFeatures { buildConfig = false }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions { jvmTarget = "1.8" }
}

dependencies { implementation(deps.androidx.annotation) }
