package com.fx.persistence

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.images.builder.Transferable
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * 실제 PostgreSQL 컨테이너를 띄워 DataSource 로 연결한다. (Docker 필요)
 * 운영과 같은 이미지를 쓰고, 운영 DB 처럼 Flyway 보다 먼저 pgvector 확장을 만들어 둔다.
 */
@TestConfiguration(proxyBeanMethods = false)
class PostgreSqlContainerConfig {

    @Bean
    @ServiceConnection
    fun postgreSqlContainer(): PostgreSQLContainer =
        PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE).asCompatibleSubstituteFor("postgres"))
            .withCopyToContainer(
                Transferable.of("CREATE EXTENSION IF NOT EXISTS vector;"),
                "/docker-entrypoint-initdb.d/01-create-extension.sql",
            )

    companion object {
        /** PostgreSQL 18 + pgvector */
        const val POSTGRES_IMAGE = "pgvector/pgvector:0.8.7-pg18"
    }

}
