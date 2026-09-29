package com.fx.migration

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

/**
 * 레거시 MongoDB 데이터를 MySQL 로 옮기는 일회용 러너.
 * 대상 스키마를 Flyway 로 만든 뒤 이관 · 건수 검증을 하고, 검증에 실패하면 종료 코드 1 로 끝난다.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class MigrationApplication

fun main(args: Array<String>) {
    exitProcess(SpringApplication.exit(runApplication<MigrationApplication>(*args)))
}
