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
        targetSdk = 31
        versionCode = 21
        versionName = "2.3.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resourceConfigurations.let {
            it.add("en")
            it.add("zh")
        }

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

    buildFeatures {
        viewBinding = true
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${rootProject.extra.get("kotlin_version")}")
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    implementation("me.weishu:free_reflection:3.0.1")
    implementation("com.github.kirich1409:viewbindingpropertydelegate-noreflection:1.4.7")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

tasks.create("jsonCompress") {
    doLast {
        val jsonFile = file("src/main/assets/open_source.json")
        val jsonElement = com.google.gson.JsonParser.parseString(jsonFile.readText())
        val gson = com.google.gson.GsonBuilder().create()
        val jsonStr = gson.toJson(jsonElement)
        // jsonFile.delete()
        jsonFile.writeText(jsonStr)
        println("JSON Compress Completed!")
    }
}

tasks.create<Exec>("pgyer") {
    inputs.properties(
        "user_key" to keystoreProperties["pgyer.user_key"],
        "api_key" to keystoreProperties["pgyer.api_key"]
    )
}

afterEvaluate {
    val assemble = tasks.findByName("assembleRelease") as Task
    val pgyer = tasks.findByName("pgyer") as Exec
    assemble.mustRunAfter("clean")
    pgyer.dependsOn("clean", assemble)
    // pgyer.dependsOn(assemble)
    assemble.doLast {
        val tree = fileTree("build/outputs/apk/release") {
            include("*.apk")
        }
        val apkFile = tree.singleFile
        pgyer.commandLine(
            "curl", "-F", "file=@${apkFile.absolutePath}",
            "-F", "uKey=${pgyer.inputs.properties["user_key"]}",
            "-F", "_api_key=${pgyer.inputs.properties["api_key"]}",
            "https://www.pgyer.com/apiv1/app/upload"
        )
    }
}
