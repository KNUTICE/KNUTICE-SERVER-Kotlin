val commonApiVersion = rootProject.extra["commonApiVersion"] as String
val jsoupVersion = rootProject.extra["jsoupVersion"] as String
val slackApiVersion = rootProject.extra["slackApiVersion"] as String
val ktorVersion = rootProject.extra["ktorVersion"] as String

dependencies {
    implementation(project(":common"))
    implementation(project(":persistence-common"))

    implementation("io.github.seob7:common-api:$commonApiVersion")

    // Ktor Client (논블로킹 HTTP)
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    // Ktor JSON Serialization
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

    // Web (Boot 4 : spring-boot-starter-web 은 deprecated)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // Jsoup (Crawler)
    implementation("org.jsoup:jsoup:$jsoupVersion")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Slack
    implementation("com.slack.api:slack-api-client:$slackApiVersion")
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
