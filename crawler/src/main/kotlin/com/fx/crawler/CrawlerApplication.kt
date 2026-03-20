package com.fx.crawler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import reactor.blockhound.BlockHound

@EnableScheduling
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories(basePackages = ["com.fx"])
@ComponentScan(basePackages = ["com.fx.crawler", "com.fx.crawler.adapter", "com.fx.global", "com.fx.readingroom"])
@SpringBootApplication
class CrawlerApplication

fun main(args: Array<String>) {
    BlockHound.builder()
        .allowBlockingCallsInside("java.io.FileInputStream", "readBytes")
        .install()
    runApplication<CrawlerApplication>(*args)
}
