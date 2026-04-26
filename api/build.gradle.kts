plugins {
    id("org.springframework.boot")
    id("com.google.devtools.ksp")
}

extra["springCloudVersion"] = "2025.0.0"

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

    // OpenFeign
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Jsoup (Crawler)
    implementation("org.jsoup:jsoup:1.21.2")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // Prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // QueryDSL - 2026-04-25 : 7.1 로 변경
    implementation("io.github.openfeign.querydsl:querydsl-mongodb:7.1") {
        exclude(group = "org.mongodb", module = "mongo-java-driver")
    }
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.1")
    implementation("com.mysema.commons:mysema-commons-lang:0.2.4")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.9.1")
    testImplementation("io.mockk:mockk:1.14.5")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
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

tasks.bootJar { enabled = false }
tasks.jar { enabled = false }

// QueryDSL QClass 생성 경로
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}