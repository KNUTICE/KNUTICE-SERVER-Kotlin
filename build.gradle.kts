import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	// Kotlin
	kotlin("jvm") version "2.3.21" apply false
	kotlin("plugin.spring") version "2.3.21" apply false
	kotlin("plugin.jpa") version "2.3.21" apply false

	// Spring Boot
	id("org.springframework.boot") version "4.1.1" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false

	// QueryDSL Q클래스 생성
	id("com.google.devtools.ksp") version "2.3.12" apply false
}

allprojects {
	group = "com.fx"
	version = "0.0.1-SNAPSHOT"
	repositories {
		mavenCentral()
	}
}

// 버전 전역 관리 (Spring Boot BOM 이 관리하지 않는 라이브러리)
extra["commonApiVersion"] = "0.0.2"
extra["querydslVersion"] = "7.7"
extra["hypersistenceUtilsVersion"] = "3.16.0"
extra["springAiVersion"] = "2.0.1"
extra["springdocVersion"] = "3.1.1"
extra["firebaseAdminVersion"] = "9.11.0"
extra["jsoupVersion"] = "1.23.2"
extra["commonsTextVersion"] = "1.15.0"
extra["slackApiVersion"] = "1.51.0"
extra["jjwtVersion"] = "0.13.0"
extra["kotestVersion"] = "6.2.5"
extra["mockkVersion"] = "1.14.11"
extra["mysemaCommonsLangVersion"] = "0.2.4"

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "com.google.devtools.ksp")
	apply(plugin = "io.spring.dependency-management")

	configure<JavaPluginExtension> {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(25))
		}
	}

	dependencies {
		// Boot 4 기본 JSON 라이브러리는 Jackson 3 (tools.jackson)
		"implementation"("tools.jackson.module:jackson-module-kotlin")
		"implementation"("org.jetbrains.kotlin:kotlin-reflect")
		"testImplementation"("org.springframework.boot:spring-boot-starter-test")
	}

	// 기본적으로 bootJar 는 끄고 각 모듈에서 필요한 경우에만 켜도록 설정
	tasks.withType<BootJar> {
		enabled = false
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

}
