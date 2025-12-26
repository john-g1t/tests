plugins {
    id("java")
    id("war")
    application
}

group = "com.github.john_g1t"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.8")
    compileOnly("jakarta.servlet:jakarta.servlet-api:5.0.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
}

application {
    mainClass = "com.github.john_g1t.Main"
}