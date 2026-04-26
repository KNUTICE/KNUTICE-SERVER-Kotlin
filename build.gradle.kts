plugins {
	kotlin("jvm") version "1.9.25" apply false
	kotlin("plugin.spring") version "1.9.25" apply false
	kotlin("plugin.jpa") version "1.9.25" apply false
	id("org.springframework.boot") version "3.5.5" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
	id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false
}

allprojects {
	group = "com.fx"
	version = "0.0.1-SNAPSHOT"
	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "com.google.devtools.ksp")
	apply(plugin = "io.spring.dependency-management")

	configure<JavaPluginExtension> {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(21))
		}
	}

	dependencies {
		"implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
		"implementation"("org.jetbrains.kotlin:kotlin-reflect")
		"testImplementation"("org.springframework.boot:spring-boot-starter-test")
	}

	// 기본적으로 bootJar 는 끄고 각 모듈에서 필요한 경우에만 켜도록 설정
	tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
		enabled = false
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

}