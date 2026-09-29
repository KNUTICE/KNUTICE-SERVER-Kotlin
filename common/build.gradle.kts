val commonApiVersion = rootProject.extra["commonApiVersion"] as String
val querydslVersion = rootProject.extra["querydslVersion"] as String
val slackApiVersion = rootProject.extra["slackApiVersion"] as String

dependencies {
    // 영속성 · QueryDSL
    implementation(project(":persistence-common"))

    implementation("io.github.seob7:common-api:$commonApiVersion")

    // Web (Boot 4 : spring-boot-starter-web 은 deprecated)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Slack
    implementation("com.slack.api:slack-api-client:$slackApiVersion")

    // QueryDSL Q클래스 생성
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:$querydslVersion")

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

// QueryDSL QClass 생성 경로
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
