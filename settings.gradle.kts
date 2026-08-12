pluginManagement {
    includeBuild("plugin")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("io.github.gmazzo.publications.report")
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "gradle-report-publications-plugin"

includeBuild("demo/build-conventions")
includeBuild("demo/build-logic")
include("demo")
include("demo:module1")
include("demo:module2")
