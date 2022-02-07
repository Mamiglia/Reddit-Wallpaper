val javaVersion = JavaVersion.VERSION_11


plugins {
    id("java")
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

    runtimeOnly("com.h2database:h2:1.4.200")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
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
