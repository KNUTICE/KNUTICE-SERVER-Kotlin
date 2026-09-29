package com.fx.common

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/** common 은 실행 모듈이 아니므로 슬라이스 테스트용 부트 설정을 테스트 소스에 둔다. */
@SpringBootApplication
@EnableJpaAuditing
class CommonTestApplication
