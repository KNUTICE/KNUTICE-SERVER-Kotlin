val commonApiVersion = rootProject.extra["commonApiVersion"] as String
val querydslVersion = rootProject.extra["querydslVersion"] as String
val springdocVersion = rootProject.extra["springdocVersion"] as String
val jsoupVersion = rootProject.extra["jsoupVersion"] as String
val commonsTextVersion = rootProject.extra["commonsTextVersion"] as String
val jjwtVersion = rootProject.extra["jjwtVersion"] as String
val ktorVersion = rootProject.extra["ktorVersion"] as String
val kotestVersion = rootProject.extra["kotestVersion"] as String
val mockkVersion = rootProject.extra["mockkVersion"] as String

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

    // RestClient + HTTP Interface (crawler 호출, OpenFeign 대체)
    implementation("org.springframework.boot:spring-boot-starter-restclient")

    // Jsoup (Crawler)
    implementation("org.jsoup:jsoup:$jsoupVersion")

    // Apache text (MealRemoteAdapter HTML unescape. 기존엔 다른 라이브러리의 전이 의존으로 들어오던 것을 명시)
    implementation("org.apache.commons:commons-text:$commonsTextVersion")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    implementation("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    implementation("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // Coroutine (버전은 Spring Boot BOM 이 관리)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

    // Prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // QueryDSL Q클래스 생성 (Mongo · QueryDSL 라이브러리는 persistence-common)
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:$querydslVersion")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
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

// QueryDSL QClass 생성 경로
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
