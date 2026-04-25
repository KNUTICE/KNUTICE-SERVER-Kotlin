plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
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

    // Mongo
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")

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

    // QueryDSL - 2026-04-25 : 7.1 로 변경
    implementation("io.github.openfeign.querydsl:querydsl-mongodb:7.1") {
        exclude(group = "org.mongodb", module = "mongo-java-driver")
    }
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.1")

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

// QueryDSL QClass 생성 경로
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}