dependencies {
    implementation(project(":global"))

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

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Slack
    implementation("com.slack.api:slack-api-client:1.45.4")
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

// Root 설정에서 false 로 설정함
//tasks.bootJar { enabled = false }
tasks.jar { enabled = true }