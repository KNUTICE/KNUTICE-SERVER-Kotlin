package com.fx.common.config.clock

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

/** 시각에 의존하는 로직(캐시 만료, 스케줄 판단 등)이 테스트에서 시계를 바꿔 끼울 수 있도록 빈으로 둔다. */
@Configuration
class ClockConfig {

    @Bean
    fun clock(): Clock =
        Clock.systemDefaultZone()

}
