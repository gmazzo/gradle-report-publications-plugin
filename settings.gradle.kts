plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "gradle-report-publications-plugin"

includeBuild("plugin")

include("demo")
includeBuild("demo/build-logic")
include("demo:module1")
include("demo:module2")
