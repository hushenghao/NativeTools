// Top-level build file where you can add configuration options common to all sub-projects/modules.

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        jcenter()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
