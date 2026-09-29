package com.fx.persistence

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.utility.DockerImageName

/** 실제 MySQL 컨테이너를 띄워 DataSource 로 연결한다. (Docker 필요) */
@TestConfiguration(proxyBeanMethods = false)
class MySqlContainerConfig {

    @Bean
    @ServiceConnection
    fun mySqlContainer(): MySQLContainer =
        MySQLContainer(DockerImageName.parse(MYSQL_IMAGE))

    companion object {
        const val MYSQL_IMAGE = "mysql:8.4"
    }

}
