plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samWithReceiver)
    `java-gradle-plugin`
}

samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)

gradlePlugin {
    plugins {
        create("report-publications") {
            id = "io.github.gmazzo.gradle.publications.report"
            implementationClass = "io.github.gmazzo.gradle.publications.report.ReportPublicationsPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
    testImplementation(gradleKotlinDsl())
}

testing.suites.withType<JvmTestSuite> {
    useKotlinTest()
}
