package com.fx.api.config.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * QueryDSL 쿼리 생성기 빈 등록.
 * 주입되는 `EntityManager` 는 트랜잭션마다 실제 영속성 컨텍스트로 위임하는 공유 프록시다.
 */
@Configuration
class QuerydslConfig(
    private val entityManager: EntityManager,
) {

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory =
        JPAQueryFactory(entityManager)

}
