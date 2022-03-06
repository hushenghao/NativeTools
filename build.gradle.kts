import com.tinify.Tinify
import java.io.FileNotFoundException

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(deps.android.gradle)
        classpath(deps.kotlin.gradle)
        classpath(deps.diffplug.spotless)
        classpath(deps.google.services)
        classpath(deps.bundles.firebase.gradle)
        classpath(deps.tinify)
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

task<Task>("tinify") {
    description = "Tinify Compressing images"
    val images = fileTree("app/src/main") {
        include("ic_launcher-playstore.png", "res/mipmap-*/ic_launcher*.png")
    }
    doFirst {
        Tinify.setKey(project.property("tinify.key").toString())
        Tinify.setAppIdentifier("Gradle task")
        for (image in images) {
            if (!image.exists()) {
                throw FileNotFoundException(image.path)
            }
            Tinify.fromFile(image.path).toFile(image.path)
        }
    }
}