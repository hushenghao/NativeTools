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
        classpath(deps.firebase.crashlytics.gradle)
        classpath(deps.firebase.perf)
        classpath(deps.tinify)
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

task<Task>("tinify") {
    description = "Tinify Compressing images"
    val images = arrayOf(
        "app/src/main/res/mipmap-hdpi/ic_launcher.png",
        "app/src/main/res/mipmap-hdpi/ic_launcher_round.png",
        "app/src/main/res/mipmap-mdpi/ic_launcher.png",
        "app/src/main/res/mipmap-mdpi/ic_launcher_round.png",
        "app/src/main/res/mipmap-xhdpi/ic_launcher.png",
        "app/src/main/res/mipmap-xhdpi/ic_launcher_round.png",
        "app/src/main/res/mipmap-xxhdpi/ic_launcher.png",
        "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png",
        "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
        "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png",
        "app/src/main/ic_launcher-playstore.png",
    )
    doFirst {
        Tinify.setKey(project.property("tinify.key").toString())
        Tinify.setAppIdentifier("Gradle task")
        var file: File
        for (image in images) {
            file = file(image)
            if (!file.exists()) {
                throw FileNotFoundException(file.path)
            }
            Tinify.fromFile(file.path).toFile(file.path)
        }
    }
}