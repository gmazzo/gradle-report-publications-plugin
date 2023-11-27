val pluginBuild = gradle.includedBuild("plugin")

tasks.register(LifecycleBasePlugin.BUILD_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.register(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.register(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.register(MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}
