![GitHub](https://img.shields.io/github/license/gmazzo/gradle-report-publications-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.gmazzo.publications.report)](https://plugins.gradle.org/plugin/io.github.gmazzo.publications.report)
[![Build Status](https://github.com/gmazzo/gradle-report-publications-plugin/actions/workflows/build.yaml/badge.svg)](https://github.com/gmazzo/gradle-report-publications-plugin/actions/workflows/build.yaml)
[![Coverage](https://codecov.io/gh/gmazzo/gradle-report-publications-plugin/branch/main/graph/badge.svg?token=D5cDiPWvcS)](https://codecov.io/gh/gmazzo/gradle-report-publications-plugin)
[![Users](https://img.shields.io/badge/users_by-Sourcegraph-purple)](https://sourcegraph.com/search?q=content:io.github.gmazzo.publications.report+-repo:github.com/gmazzo/gradle-report-publications-plugin)

[![Contributors](https://contrib.rocks/image?repo=gmazzo/gradle-report-publications-plugin)](https://github.com/gmazzo/gradle-report-publications-plugin/graphs/contributors)

# gradle-report-publications-plugin

A Gradle plugin that decorates the build logs with maven coordinates of artifacts published with `publish` or
`publishToMavenLocal`

# Usage

Apply the plugin at the **root** project (preferable):

```kotlin
plugins {
    id("io.github.gmazzo.publications.report") version "<latest>"
}
```

Then, whenever you call Maven Publish task (usually `publish` or `publishToMavenLocal`) on any project, the plugin will
decorate the build logs showing maven coordinates (`GAV`):

For instance, the `demo` project will print for `publishToMavenLocal`:

```
> Task :publishToMavenLocal

The following artifacts were published to mavenLocal(~/.m2/repository):
 - io.gmazzo.demo:demo:0.1.0 jar
 - io.gmazzo.demo:module1:0.1.0 jar
 - io.gmazzo.demo:module2:0.1.0 jar
 - io.gmazzo.demo.build-logic:build-logic:0.1.0 jar
 - io.gmazzo.demo.build-logic:otherModule:0.1.0 jar
```

![`./gradlew publishToMavenLocal` output example](README-example-output.png)
> [!NOTE]
> This plugin can gather publications from `includedBuild`s too.
> But it also needs to be applied at the root project of the `includedBuild` to work properly.
