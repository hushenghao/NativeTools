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
        classpath(deps.google.services)
        classpath(deps.firebase.crashlytics.gradle)
        classpath(deps.firebase.perf)
        classpath(deps.diffplug.spotless)
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
