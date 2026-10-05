package com.fx.common.annotation

import org.springframework.stereotype.Repository

/** 영속성 어댑터. `@Repository` 이므로 JPA · JDBC 예외가 Spring `DataAccessException` 으로 바뀐다. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repository
annotation class PersistenceAdapter()
