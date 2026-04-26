dependencies {

    implementation("io.github.seob7:common-api:0.0.2")

    // Mongo
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Slack
    implementation("com.slack.api:slack-api-client:1.45.4")

    // QueryDSL - 2026-04-25 : 7.1 로 변경
    implementation("io.github.openfeign.querydsl:querydsl-mongodb:7.1") {
        exclude(group = "org.mongodb", module = "mongo-java-driver")
    }
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.1")
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