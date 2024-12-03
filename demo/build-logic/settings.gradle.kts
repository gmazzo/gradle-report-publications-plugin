dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    val catalog = files("../../gradle/libs.versions.toml")
    if (catalog.asFileTree.any()) {
        versionCatalogs {
            create("libs") {
                from(catalog)
            }
        }
    }
}

include("otherModule")
