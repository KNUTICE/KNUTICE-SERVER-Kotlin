package com.fx.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = ["com.fx.api.adapter.out.persistence.repository"])
@SpringBootApplication(scanBasePackages = ["com.fx.api", "com.fx.api.adapter"])
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
