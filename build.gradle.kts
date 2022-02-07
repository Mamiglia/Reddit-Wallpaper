val javaVersion = JavaVersion.VERSION_11


plugins {
    id("java")
}

group = "com.mamiglia"
version = "0.0.4" // todo: What is the correct version?
description = "Reddit-Wallpaper"
java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion



repositories {
    mavenCentral()
}

dependencies {
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
