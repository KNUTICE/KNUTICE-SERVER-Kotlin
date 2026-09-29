plugins {
    id("java-library")
}

val querydslVersion = rootProject.extra["querydslVersion"] as String
val mysemaCommonsLangVersion = rootProject.extra["mysemaCommonsLangVersion"] as String

dependencies {
    // MongoDB
    api("org.springframework.boot:spring-boot-starter-data-mongodb")

    // QueryDSL (Q클래스 생성은 도큐먼트가 있는 모듈에서 ksp 로 한다)
    api("io.github.openfeign.querydsl:querydsl-mongodb:$querydslVersion") {
        exclude(group = "org.mongodb", module = "mongo-java-driver")
    }
    api("com.mysema.commons:mysema-commons-lang:$mysemaCommonsLangVersion")
}

tasks.jar { enabled = true }
