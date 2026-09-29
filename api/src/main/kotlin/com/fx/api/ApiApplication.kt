package com.fx.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@EntityScan(basePackages = ["com.fx"]) // 엔티티가 common · reading-room 모듈에도 있다
@EnableJpaRepositories(basePackages = ["com.fx"])
// scanBasePackages 로 지정해야 @DataJpaTest 같은 슬라이스 테스트의 컴포넌트 필터가 적용된다
@SpringBootApplication(scanBasePackages = ["com.fx.api", "com.fx.common", "com.fx.readingroom"])
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
