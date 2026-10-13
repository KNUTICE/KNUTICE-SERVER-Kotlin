package com.fx.api.config.apilog

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * 앱 API 요청 로그 수집 설정 (`api-log.*`).
 *
 * @property enabled 수집 여부. 끄면 필터 · 버퍼 · 저장 스케줄러를 등록하지 않는다
 * @property bufferCapacity 저장 전에 메모리에 쌓아 두는 최대 건수. 가득 차면 새 로그를 버린다 (DB 장애 때 메모리가 계속 늘지 않도록)
 * @property batchSize 한 번에 저장하는 건수 (트랜잭션 하나, JDBC 배치 INSERT)
 * @property flushInterval 버퍼를 비우는 주기
 */
@ConfigurationProperties("api-log")
data class ApiLogProperties(
    val enabled: Boolean = true,
    val bufferCapacity: Int = 10_000,
    val batchSize: Int = 500,
    val flushInterval: Duration = Duration.ofSeconds(1),
)
