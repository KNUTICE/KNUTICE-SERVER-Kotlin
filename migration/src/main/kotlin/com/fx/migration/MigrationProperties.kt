package com.fx.migration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.ZoneId

/**
 * @property legacyZone 레거시 앱이 `LocalDateTime` · `LocalDate` 를 `Date` 로 바꿀 때 쓴 시스템 타임존
 * @property chunkSize 한 트랜잭션에 쓰는 도큐먼트 수
 * @property runOnStartup 기동하자마자 이관할지. 테스트에서는 끈다
 */
@ConfigurationProperties("migration")
data class MigrationProperties(
    val legacyZone: ZoneId = ZoneId.of("Asia/Seoul"),
    val chunkSize: Int = 1000,
    val runOnStartup: Boolean = true,
)
