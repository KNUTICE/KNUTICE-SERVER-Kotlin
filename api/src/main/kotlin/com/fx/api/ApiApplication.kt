package com.fx.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableJpaAuditing
@EnableMongoAuditing
@EntityScan(basePackages = ["com.fx"]) // 엔티티가 common · reading-room 모듈에도 있다
@EnableJpaRepositories(basePackages = ["com.fx"])
@EnableMongoRepositories(basePackages = ["com.fx"])
@ComponentScan(basePackages = ["com.fx.api", "com.fx.api.adapter", "com.fx.common", "com.fx.readingroom"])
@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
