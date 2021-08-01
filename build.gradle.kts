tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

buildscript {
    extra.apply {
        set("kotlin_version", "1.5.21")
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra.get("kotlin_version")}")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        jcenter()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
