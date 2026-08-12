plugins {
    id("demo-module-convention")
    `java-library`
    `maven-publish`
    signing
}

tasks.withType<AbstractPublishToMaven> {
    enabled = false
}
