plugins {
    kotlin("jvm") version "2.1.20"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.probendi.aris"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.2")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.probendi.aris.ArisKt"
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.probendi.aris.ArisKt")
}
