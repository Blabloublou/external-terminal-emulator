plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "terminal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("terminal.demo.InteractiveDemoKt")
}

tasks.test {
    useJUnitPlatform()
}
