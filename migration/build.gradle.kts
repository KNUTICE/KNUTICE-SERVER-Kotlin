// MongoDB → MySQL 일회용 이관 러너

dependencies {
    // 이관 대상 스키마의 상태 값 · 컬럼 길이 (DeviceType, NotificationStatus 등)
    implementation(project(":common"))

    // MySQL · JDBC · Flyway · TSID
    implementation(project(":persistence-common"))

    // 레거시 MongoDB 읽기. 다른 모듈은 MongoDB 를 쓰지 않으므로 이 모듈에만 둔다
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Test : 실제 MongoDB · MySQL(Testcontainers)로 이관 전 과정을 검증
    testImplementation(testFixtures(project(":persistence-common")))
    testImplementation("org.testcontainers:testcontainers-mongodb")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.bootJar { enabled = true }
tasks.jar { enabled = false }
