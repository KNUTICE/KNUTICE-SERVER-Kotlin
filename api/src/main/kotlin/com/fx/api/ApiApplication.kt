package com.fx.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling
//import reactor.blockhound.BlockHound

@EnableScheduling
@EnableMongoAuditing
@EnableReactiveMongoRepositories(basePackages = ["com.fx"])
@ComponentScan(basePackages = ["com.fx.api", "com.fx.api.adapter", "com.fx.global", "com.fx.readingroom"])
@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
//    BlockHound.builder()
//        .allowBlockingCallsInside("java.io.FileInputStream", "readBytes")
//        .install()
    runApplication<ApiApplication>(*args)
}
