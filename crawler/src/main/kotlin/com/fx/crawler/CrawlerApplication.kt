package com.fx.crawler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@ConfigurationPropertiesScan
@EntityScan(basePackages = ["com.fx"]) // 엔티티가 common · reading-room 모듈에도 있다
@EnableJpaRepositories(basePackages = ["com.fx"])
@SpringBootApplication(scanBasePackages = ["com.fx.crawler", "com.fx.common", "com.fx.readingroom"])
class CrawlerApplication

fun main(args: Array<String>) {
    runApplication<CrawlerApplication>(*args)
}
