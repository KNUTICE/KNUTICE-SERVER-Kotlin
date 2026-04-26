plugins {
    id("org.springframework.boot")
    id("com.google.devtools.ksp")
}

extra["springAiVersion"] = "1.0.2"

dependencies {
    implementation(project(":global"))
    implementation(project(":reading-room"))

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

    // Gemini
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

    // Prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // 2026-04-25 : OpenFeign QueryDSL 7.1 로 변경
    implementation("io.github.openfeign.querydsl:querydsl-mongodb:7.1") {
        exclude(group = "org.mongodb", module = "mongo-java-driver")
    }
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.1")
    implementation("com.mysema.commons:mysema-commons-lang:0.2.4")
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

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

// QueryDSL QClass 생성 경로
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}