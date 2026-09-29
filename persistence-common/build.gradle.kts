plugins {
    id("java-library")
    // 다른 모듈의 영속성 테스트가 MySQL 컨테이너 설정(MySqlContainerConfig)을 함께 쓴다
    id("java-test-fixtures")
}

val querydslVersion = rootProject.extra["querydslVersion"] as String
val hypersistenceUtilsVersion = rootProject.extra["hypersistenceUtilsVersion"] as String
val mysemaCommonsLangVersion = rootProject.extra["mysemaCommonsLangVersion"] as String

dependencies {
    // JPA · MySQL
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")

    // 스키마 마이그레이션
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-mysql")

    // TSID 식별자 (Hibernate 7.3 · 7.4 용 모듈)
    api("io.hypersistence:hypersistence-utils-hibernate-73:$hypersistenceUtilsVersion")

    // QueryDSL (JPA). Q클래스 생성은 엔티티가 있는 모듈에서 ksp 로 한다
    api("io.github.openfeign.querydsl:querydsl-jpa:$querydslVersion")
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:$querydslVersion")

    // MongoDB
    api("org.springframework.boot:spring-boot-starter-data-mongodb")
    api("io.github.openfeign.querydsl:querydsl-mongodb:$querydslVersion") {
        exclude(group = "org.mongodb", module = "mongo-java-driver")
    }
    api("com.mysema.commons:mysema-commons-lang:$mysemaCommonsLangVersion")

    // Test fixtures : 실제 MySQL(Testcontainers) 컨테이너 설정
    testFixturesImplementation("org.springframework.boot:spring-boot-test")
    testFixturesApi("org.springframework.boot:spring-boot-testcontainers")
    testFixturesApi("org.testcontainers:testcontainers-mysql")

    // Test : JPA 슬라이스 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    kspTest("io.github.openfeign.querydsl:querydsl-ksp-codegen:$querydslVersion")
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

tasks.jar { enabled = true }
