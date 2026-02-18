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
val queryDslVersion = "5.1.0"
extra["springAiVersion"] = "1.0.2"

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

    // Webflux - WebClient 용도
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Firebase
    implementation("com.google.firebase:firebase-admin:9.5.0")

    // Jsoup (Crawler)
    implementation("org.jsoup:jsoup:1.21.2")

    // Apache text
    implementation("org.apache.commons:commons-text:1.14.0")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Gemini
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation(project(":global"))
    implementation(project(":reading-room"))

    // Prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // QueryDSL
    implementation("com.querydsl:querydsl-mongodb:${queryDslVersion}") {
        exclude(group = "org.mongodb", module = "mongo-java-driver")
    }
    annotationProcessor("com.querydsl:querydsl-apt:${queryDslVersion}:general")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
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
    enabled = true
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

val querydslDir = "src/main/generated"

sourceSets {
    getByName("main").java.srcDirs(querydslDir)
}

tasks.withType<JavaCompile> {
    options.generatedSourceOutputDirectory = file(querydslDir)

    // 위의 설정이 안되면 아래 설정 사용
    // options.generatedSourceOutputDirectory.set(file(querydslDir))
}

tasks.named("clean") {
    doLast {
        file(querydslDir).deleteRecursively()
    }
}