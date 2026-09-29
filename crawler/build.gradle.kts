val commonApiVersion = rootProject.extra["commonApiVersion"] as String
val springAiVersion = rootProject.extra["springAiVersion"] as String
val firebaseAdminVersion = rootProject.extra["firebaseAdminVersion"] as String
val jsoupVersion = rootProject.extra["jsoupVersion"] as String
val commonsTextVersion = rootProject.extra["commonsTextVersion"] as String
val mockkVersion = rootProject.extra["mockkVersion"] as String

dependencies {
    implementation(project(":common"))
    implementation(project(":persistence-common"))
    implementation(project(":reading-room"))

    implementation("io.github.seob7:common-api:$commonApiVersion")

    // Spring Batch (JobRepository 는 MySQL 의 BATCH_* 테이블)
    implementation("org.springframework.boot:spring-boot-starter-batch-jdbc")

    // Web (Boot 4 : spring-boot-starter-web 은 deprecated)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // RestClient (학교 게시판 · 식단 사이트 호출)
    implementation("org.springframework.boot:spring-boot-starter-restclient")

    // Firebase
    implementation("com.google.firebase:firebase-admin:$firebaseAdminVersion")

    // Jsoup (Crawler)
    implementation("org.jsoup:jsoup:$jsoupVersion")

    // Apache text
    implementation("org.apache.commons:commons-text:$commonsTextVersion")

    // Gemini (Spring AI 2.0 : OpenAI 공식 Java SDK 기반)
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Test
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation(testFixtures(project(":persistence-common")))
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
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
