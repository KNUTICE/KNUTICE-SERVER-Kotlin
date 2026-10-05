val commonApiVersion = rootProject.extra["commonApiVersion"] as String
val jsoupVersion = rootProject.extra["jsoupVersion"] as String
val slackApiVersion = rootProject.extra["slackApiVersion"] as String

dependencies {
    implementation(project(":common"))
    implementation(project(":persistence-common"))

    implementation("io.github.seob7:common-api:$commonApiVersion")

    // RestClient (열람실 사이트 호출)
    implementation("org.springframework.boot:spring-boot-starter-restclient")

    // Web (Boot 4 : spring-boot-starter-web 은 deprecated)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // Jsoup (CSRF 토큰 추출)
    implementation("org.jsoup:jsoup:$jsoupVersion")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Slack
    implementation("com.slack.api:slack-api-client:$slackApiVersion")

    // Test : 실제 MySQL(Testcontainers)로 JPA 슬라이스 테스트
    testImplementation(testFixtures(project(":persistence-common")))
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
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
