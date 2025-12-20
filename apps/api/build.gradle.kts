plugins {
    id("java")
    application
}

group = "com.github.john_g1t"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.8")
}

application {
    mainClass = "com.github.john_g1t.Main"
}