plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "ch.typedef"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // Testing
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
}

kotlin {
    jvmToolchain(23)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

application {
    mainClass.set("ch.typedef.swekt.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "swekt",
            "Implementation-Version" to "0.0.1",
            "Implementation-Vendor" to "Urs Stotz - Entwicklung"
        )
    }
}
