import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Properties

val keystoreProperties = Properties().apply {
    rootProject.file("key.properties")
        .takeIf { it.exists() }?.inputStream()?.use(this::load)
}

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.diffplug.spotless")
}

android {
    compileSdk = 31
    buildToolsVersion = "31.0.0"
    defaultConfig {
        applicationId = "com.dede.nativetools"
        minSdk = 23
        targetSdk = 30
        versionCode = 52
        versionName = "3.6.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resourceConfigurations.addAll(
            listOf(
                "zh-rCN",
                "zh-rHK",
                "ja-rJP",
                "en-rUS",
                "ko-rKR",
                "ru-rRU",
                "de-rDE",
                "fr-rFR",
                "es-rES"
            )
        )

        // rename output file name
        // https://stackoverflow.com/a/52508858/10008797
        setProperty("archivesBaseName", "native_tools_${versionName}_$versionCode")
    }

    signingConfigs {
        if (keystoreProperties.isEmpty) return@signingConfigs
        create("release") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        val config = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        getByName("debug") {
            versionNameSuffix = "-debug"
            signingConfig = config
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = config
        }
        create("beta") {
            initWith(getByName("release"))
            versionNameSuffix = "-beta"
        }
    }

    viewBinding {
        isEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(deps.kotlin.stdlib)
    implementation(deps.androidx.appcompat)
    implementation(deps.androidx.core.ktx)
    implementation(deps.androidx.preference.ktx)
    implementation(deps.androidx.recyclerview)
    implementation(deps.bundles.androidx.lifecycle)
    implementation(deps.bundles.androidx.navigation)
    implementation(deps.google.material)
    implementation(deps.androidx.browser)
    implementation(deps.androidx.startup)
    implementation(deps.androidx.work.runtime.ktx)

    implementation(deps.free.reflection)
    implementation(deps.viewbinding.property.delegate)

    debugImplementation(deps.bundles.squareup.leakcanary)

    testImplementation(deps.junit)
    androidTestImplementation(deps.bundles.androidx.test)
}

spotless {
    java {
        googleJavaFormat()
    }
    kotlin {
        ktfmt()
        ktlint()
        diktat()
        prettier()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

configurations.all {
    exclude("androidx.viewpager2", "viewpager2")
    exclude("androidx.viewpager", "viewpager")
    exclude("androidx.slidingpanelayout", "slidingpanelayout")
    exclude("androidx.swiperefreshlayout", "swiperefreshlayout")
    exclude("androidx.dynamicanimation", "dynamicanimation")
    exclude("androidx.localbroadcastmanager", "localbroadcastmanager")
    exclude("androidx.documentfile", "documentfile")
    exclude("androidx.print", "print")
    exclude("androidx.cursoradapter", "cursoradapter")
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
}

tasks.register<Exec>("pgyer") {
    val apiKey = checkNotNull(keystoreProperties["pgyer.api_key"]) {
        "pgyer.api_key not found"
    }

    val assemble = tasks.named("assembleBeta").get()
    dependsOn("clean", assemble)
    assemble.mustRunAfter("clean")

    val tree = fileTree("build/intermediates/apk/beta") {
        include("*.apk")
        builtBy("assembleBeta")
    }
    doFirst {
        val apkPath = tree.single().absolutePath
        println("Upload Apk: $apkPath")

        commandLine(
            "curl", "-F", "file=@$apkPath",
            "-F", "_api_key=$apiKey",
            "-F", "buildUpdateDescription=Upload by gradle pgyer task",
            "https://www.pgyer.com/apiv2/app/upload"
        )
    }
    val output = ByteArrayOutputStream().apply {
        standardOutput = this
    }
    doLast {
        val result = output.toString()
        val obj = JSONObject(result)
        if (obj.getInt("code") == 0) {
            val path = obj.getJSONObject("data")
                .getString("buildShortcutUrl")
            println("Upload succeeded: https://www.pgyer.com/$path")
        } else {
            val message = obj.getString("message")
            println("Upload failed: $message")
        }
    }
}
