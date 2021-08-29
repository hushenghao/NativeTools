include("app")
rootProject.name = "NativeTools"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            // default path: gradle/libs.versions.toml
            from(files("libs.versions.toml"))
        }
    }
}