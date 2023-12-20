plugins {
    `java-library`
    `maven-publish`
}

tasks.withType<AbstractPublishToMaven>() {
    enabled = false
}
