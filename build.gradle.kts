tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(deps.bundles.classpath)
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
