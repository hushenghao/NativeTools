import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*

val keystoreProperties = Properties().apply {
    rootProject.file("key.properties")
        .takeIf { it.exists() }?.inputStream()?.use(this::load)
}

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    compileSdk = 31
    buildToolsVersion = "31.0.0"
    defaultConfig {
        applicationId = "com.dede.nativetools"
        minSdk = 23
        targetSdk = 30
        versionCode = 30
        versionName = "2.6.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resourceConfigurations.addAll(arrayListOf("en", "zh"))

        // rename output file name
        // https://stackoverflow.com/a/52508858/10008797
        setProperty("archivesBaseName", "native_tools_${versionName}_${versionCode}")
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
    }
}

dependencies {
    implementation(deps.kotlin.stdlib)
    implementation(deps.appcompat)
    implementation(deps.material)
    implementation(deps.preference.ktx)
    implementation(deps.bundles.navigation)

    implementation(deps.free.reflection)
    implementation(deps.viewbinding.property.delegate)

    debugImplementation(deps.leakcanary)

    testImplementation(deps.junit)
    androidTestImplementation(deps.bundles.android.test)
}

configurations.all {
    //exclude("androidx.drawerlayout", "drawerlayout")
    //exclude("androidx.coordinatorlayout", "coordinatorlayout")
    exclude("androidx.viewpager2", "viewpager2")
    exclude("androidx.viewpager", "viewpager")
    exclude("androidx.slidingpanelayout", "slidingpanelayout")
    exclude("androidx.swiperefreshlayout", "swiperefreshlayout")
    exclude("androidx.asynclayoutinflater", "asynclayoutinflater")
    exclude("androidx.transition", "transition")
    exclude("androidx.dynamicanimation", "dynamicanimation")
    //exclude("androidx.vectordrawable", "vectordrawable-animated")
    exclude("androidx.versionedparcelable", "versionedparcelable")
    exclude("androidx.localbroadcastmanager", "localbroadcastmanager")
    exclude("androidx.documentfile", "documentfile")
    exclude("androidx.print", "print")
    exclude("androidx.cursoradapter", "cursoradapter")
}

tasks.register<Exec>("pgyer") {
    val apiKey = keystoreProperties["pgyer.api_key"]
        ?: throw IllegalArgumentException("pgyer.api_key not found")

    val assemble = tasks.named("assembleRelease").get()
    dependsOn("clean", assemble)
    assemble.mustRunAfter("clean")

    val tree = fileTree("build/outputs/apk/release") {
        include("*.apk")
        builtBy("assembleRelease")
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
            println("Uploaded successfully: https://www.pgyer.com/$path")
        } else {
            val message = obj.getString("message")
            println("Upload failed: $message")
        }
    }
}
