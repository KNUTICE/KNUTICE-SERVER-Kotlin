plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.fx"
version = "0.0.1-SNAPSHOT"
description = "KNUTICE is a platform that provides push notifications for official announcements from Korea National University of Transportation. By integrating a web crawler and real-time notification system, it ensures students receive timely updates without manually checking the university website."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("io.github.seob7:common-api:0.0.2")

    // Ktor Client (논블로킹 HTTP)
    implementation("io.ktor:ktor-client-cio:2.3.6")

    // Ktor JSON Serialization
    implementation("io.ktor:ktor-client-content-negotiation:2.3.13")
    implementation("io.ktor:ktor-serialization-jackson:2.3.13")

    // Mongo
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Jsoup (Crawler)
    implementation("org.jsoup:jsoup:1.21.2")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Slack
    implementation("com.slack.api:slack-api-client:1.45.4")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation(project(":global"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}
