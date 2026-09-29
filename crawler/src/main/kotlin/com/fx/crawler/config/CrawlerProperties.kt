package com.fx.crawler.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/** crawler 배치 · 크롤링 설정 (`crawler.*`). */
@ConfigurationProperties("crawler")
data class CrawlerProperties(
    val poller: Poller = Poller(),
    val launch: Launch = Launch(),
    val push: Push = Push(),
    val crawl: Crawl = Crawl(),
    val summary: Summary = Summary(),
    val seatAlert: SeatAlert = SeatAlert(),
) {

    /**
     * @property enabled 스케줄 폴러 사용 여부. 테스트에서는 끈다
     * @property requestBatchSize 한 번에 처리하는 수동 실행 요청 수
     */
    data class Poller(
        val enabled: Boolean = true,
        val requestBatchSize: Int = 20,
    )

    /**
     * @property async Job 을 비동기로 실행할지. 끄면 폴러 · 테스트가 Job 이 끝날 때까지 기다린다
     * @property maxConcurrency 동시에 실행할 수 있는 Job 수
     */
    data class Launch(
        val async: Boolean = true,
        val maxConcurrency: Int = 10,
    )

    /**
     * @property chunkSize 발송 chunk 크기 (FCM multicast 한 번의 최대 토큰 수 500 이하)
     * @property partitionConcurrency 동시에 발송하는 토픽 수. chunk 트랜잭션이 발송하는 동안 DB 커넥션을 쥐고 있으므로 커넥션 풀보다 작게 둔다
     * @property maxAttempts FCM 일시 오류 때 같은 토큰에 보내는 최대 횟수
     */
    data class Push(
        val chunkSize: Int = 500,
        val partitionConcurrency: Int = 4,
        val maxAttempts: Int = 3,
    )

    /**
     * @property maxConcurrency 학교 사이트(게시판 · 식단)에 동시에 보내는 요청 수
     * @property timeout 크롤링 요청 묶음 전체의 제한 시간. 넘으면 남은 요청을 취소한다
     */
    data class Crawl(
        val maxConcurrency: Int = 8,
        val timeout: Duration = Duration.ofMinutes(3),
    )

    /**
     * @property chunkSize 요약 chunk 크기
     * @property maxAttempts 공지 하나의 최대 요약 시도 횟수. 도달하면 FAILED 로 두고 더 시도하지 않는다
     */
    data class Summary(
        val chunkSize: Int = 5,
        val maxAttempts: Int = 3,
    )

    /**
     * @property maxConcurrency 열람실 사이트에 동시에 보내는 좌석 조회 수
     * @property timeout 좌석 조회 묶음 전체의 제한 시간
     */
    data class SeatAlert(
        val maxConcurrency: Int = 4,
        val timeout: Duration = Duration.ofSeconds(30),
    )

}
