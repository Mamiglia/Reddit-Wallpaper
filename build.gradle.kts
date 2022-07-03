import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val javaVersion = JavaVersion.VERSION_11


plugins {
    id("java")
    kotlin("jvm") version "1.6.20-M1"
    kotlin("plugin.serialization") version "1.6.10"
    id("io.github.file5.guidesigner") version "1.0.1"
//    id("org.springframework.boot") version "2.0.1.RELEASE"
    application
}

group = "com.mamiglia"
version = "1.2.2"
description = "Reddit-Wallpaper"
java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion



repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.clojars.org")
        name = "Clojars"
    }
}

dependencies {
    implementation("net.java.dev.jna:jna-platform:5.11.0")
    implementation("org.json:json:20220320")
    implementation("com.formdev:flatlaf:2.3")

    implementation("com.jetbrains.intellij.java:java-gui-forms-rt:+")
    implementation(
        "com.jgoodies:forms:1.3.0"
    )

    runtimeOnly("com.h2database:h2:+")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.apache.logging.log4j:log4j-api:+")
    implementation("org.apache.logging.log4j:log4j-core:+")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:+")

}


application {
    mainClass.set("com.mamiglia.Main")
}

tasks {
    test {
        useJUnitPlatform()
    }

    wrapper {
        gradleVersion = "7.3.3"
        distributionType = Wrapper.DistributionType.BIN
    }

    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources")) // We need this for Gradle optimization to work
        archiveClassifier.set("standalone") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

