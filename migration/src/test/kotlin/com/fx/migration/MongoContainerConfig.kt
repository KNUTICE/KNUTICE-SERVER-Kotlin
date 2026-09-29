package com.fx.migration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.mongodb.MongoDBContainer
import org.testcontainers.utility.DockerImageName

/** 레거시 MongoDB 컨테이너. (Docker 필요) */
@TestConfiguration(proxyBeanMethods = false)
class MongoContainerConfig {

    @Bean
    @ServiceConnection
    fun mongoDbContainer(): MongoDBContainer =
        MongoDBContainer(DockerImageName.parse("mongo:8.0"))

}
