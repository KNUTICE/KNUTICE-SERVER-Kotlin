val commonApiVersion = rootProject.extra["commonApiVersion"] as String
val springAiVersion = rootProject.extra["springAiVersion"] as String
val firebaseAdminVersion = rootProject.extra["firebaseAdminVersion"] as String
val jsoupVersion = rootProject.extra["jsoupVersion"] as String
val commonsTextVersion = rootProject.extra["commonsTextVersion"] as String
val ktorVersion = rootProject.extra["ktorVersion"] as String

dependencies {
    implementation(project(":common"))
    implementation(project(":persistence-common"))
    implementation(project(":reading-room"))

    implementation("io.github.seob7:common-api:$commonApiVersion")

    // Ktor Client (논블로킹 HTTP)
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    // Ktor JSON Serialization
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

    // Web (Boot 4 : spring-boot-starter-web 은 deprecated)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // Webflux - WebClient 용도
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Firebase
    implementation("com.google.firebase:firebase-admin:$firebaseAdminVersion")

    // Jsoup (Crawler)
    implementation("org.jsoup:jsoup:$jsoupVersion")

    // Apache text
    implementation("org.apache.commons:commons-text:$commonsTextVersion")

    // Gemini (Spring AI 2.0 : OpenAI 공식 Java SDK 기반)
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Coroutine (버전은 Spring Boot BOM 이 관리)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
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

tasks.bootJar { enabled = true }
tasks.jar { enabled = false }
