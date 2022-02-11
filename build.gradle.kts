import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val javaVersion = JavaVersion.VERSION_11


plugins {
    id("java")
    kotlin("jvm") version "1.6.20-M1"
    kotlin("plugin.serialization") version "1.6.10"
    id("io.github.file5.guidesigner") version "1.0.1"
}

group = "com.mamiglia"
version = "1.2.2"
description = "Reddit-Wallpaper"
java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion



repositories {
    mavenCentral()
}

dependencies {
    implementation("net.java.dev.jna:jna-platform:5.8.0")
    implementation("org.json:json:20210307")
    implementation("com.formdev:flatlaf:1.1.2")

    implementation("com.jetbrains.intellij.java:java-gui-forms-rt:+")
    implementation("com.jgoodies:forms:1.1-preview"
    )

    runtimeOnly("com.h2database:h2:1.4.200")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}

tasks {
    test {
        useJUnitPlatform()
    }

    wrapper {
        gradleVersion = "7.3.3"
        distributionType = Wrapper.DistributionType.BIN
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}