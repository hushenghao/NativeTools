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
        classpath(deps.tinify)
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

task<Task>("tinify") {
    description = "Tinify Compressing images"
    val images = arrayOf(
        "app/src/main/ic_launcher_day-playstore.png",
        "app/src/main/ic_launcher_night-playstore.png",
    )
    doFirst {
        Tinify.setKey("1Ny9z3LtggWW7TlnVj2jNkqRRHT9LFsK")// Free plan
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