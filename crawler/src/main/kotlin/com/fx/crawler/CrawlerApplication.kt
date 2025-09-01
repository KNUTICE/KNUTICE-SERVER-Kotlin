package com.fx.crawler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = ["com.fx.crawler.adapter.out.persistence.repository"])
@ComponentScan(basePackages = ["com.fx.crawler", "com.fx.crawler.adapter", "com.fx.global"])
@SpringBootApplication
class CrawlerApplication

fun main(args: Array<String>) {
    runApplication<CrawlerApplication>(*args)
}
