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

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
