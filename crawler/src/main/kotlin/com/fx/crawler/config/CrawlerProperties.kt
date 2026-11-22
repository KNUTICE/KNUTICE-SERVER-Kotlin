package com.fx.crawler.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.unit.DataSize
import java.time.Duration

/** crawler 배치 · 크롤링 설정 (`crawler.*`). */
@ConfigurationProperties("crawler")
data class CrawlerProperties(
    val poller: Poller = Poller(),
    val launch: Launch = Launch(),
    val push: Push = Push(),
    val crawl: Crawl = Crawl(),
    val summary: Summary = Summary(),
    val embedding: Embedding = Embedding(),
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
     * @property retryInterval 요약에 실패한 공지를 다시 시도하기까지 기다리는 시간. AI 장애가 짧게 지나가는 동안 시도 횟수를 다 쓰지 않도록 한다
     * @property requestInterval AI 요청 사이의 최소 간격 (재시도 포함). 분당 요청 한도를 넘지 않도록 둔다
     * @property rateLimitCooldown 호출 한도 응답(429)을 받은 뒤 요청을 멈추는 시간
     */
    data class Summary(
        val chunkSize: Int = 5,
        val maxAttempts: Int = 3,
        val retryInterval: Duration = Duration.ofMinutes(30),
        val requestInterval: Duration = Duration.ofSeconds(5),
        val rateLimitCooldown: Duration = Duration.ofMinutes(1),
    )

    /**
     * RAG 임베딩 (Gemini 기본 API). API 키는 이 설정에 두지 않는다 (data class 의 toString 으로 새지 않도록 어댑터가 따로 읽는다).
     *
     * @property baseUrl Gemini API 주소. 요약이 쓰는 OpenAI 호환 경로는 이미지 임베딩을 받지 않아 기본 API 를 쓴다
     * @property chunkSize 임베딩 chunk 크기 (공지 수)
     * @property maxAttempts 공지 하나의 최대 임베딩 시도 횟수. 도달하면 FAILED 로 두고 더 시도하지 않는다
     * @property retryInterval 임베딩에 실패한 공지를 다시 시도하기까지 기다리는 시간
     * @property model 임베딩 모델. 벡터 차원은 테이블에 묶여 있어 설정이 아닌 `EMBEDDING_DIMENSIONS` 를 쓴다
     * @property requestInterval 입력 하나당 최소 간격 (재시도 포함). 무료 한도는 묶음 요청도 입력마다 한 번으로 세므로 입력 수만큼 기다린다
     * @property rateLimitCooldown 분당 호출 한도 응답(429)을 받은 뒤 요청을 멈추는 시간
     * @property dailyLimitCooldown 하루 호출 한도(429 의 quotaId 에 `PerDay`)를 다 쓴 뒤 요청을 멈추는 시간.
     *   멈춘 동안 실행마다 학교 사이트를 헛되이 읽지 않도록 길게 둔다
     * @property errorCooldown Gemini · 학교 사이트가 공지와 상관없는 오류(인증 · 권한 · 5xx · 네트워크)를 내면 요청을 멈추는 시간.
     *   그 오류를 처음 만난 공지만 실패로 세고, 멈춘 동안의 공지는 미룬다 (장애 동안 대기 공지가 모두 FAILED 가 되지 않도록)
     * @property maxBatchSize 한 번에 묶어 보내는 텍스트 수. 이미지는 요청 크기를 줄이려고 한 장씩 보낸다
     * @property maxImagesPerNotice 공지 하나에서 임베딩하는 이미지 수 (본문 앞쪽부터). 받으려 시도하는 이미지는 이것의 2배까지다
     * @property allowedImageHost 상세 페이지 · 이미지를 읽어도 되는 사이트. 이 도메인과 하위 도메인만 요청한다
     * @property minImageSize 이보다 작은 이미지(아이콘 · 글머리 그림)는 임베딩하지 않는다
     * @property maxImageSize 이보다 큰 상세 페이지 · 이미지는 읽다가 멈추고 임베딩하지 않는다
     * @property siteRequestInterval 학교 사이트(상세 페이지 · 이미지) 요청 사이의 최소 간격
     * @property siteTimeout 학교 사이트 연결 제한 시간, 상세 페이지 응답 제한 시간
     * @property imageTimeout 이미지 응답 제한 시간. 학교 이미지 서버는 느려서(1.3MB 에 52~72초) 길게 둔다. 넘으면 그 이미지만 건너뛴다
     */
    data class Embedding(
        val chunkSize: Int = 5,
        val maxAttempts: Int = 3,
        val retryInterval: Duration = Duration.ofMinutes(30),
        val baseUrl: String = "https://generativelanguage.googleapis.com/v1beta",
        val model: String = "gemini-embedding-2",
        val requestInterval: Duration = Duration.ofSeconds(2),
        val rateLimitCooldown: Duration = Duration.ofMinutes(1),
        val dailyLimitCooldown: Duration = Duration.ofHours(1),
        val errorCooldown: Duration = Duration.ofMinutes(10),
        val maxBatchSize: Int = 20,
        val maxImagesPerNotice: Int = 10,
        val allowedImageHost: String = "ut.ac.kr",
        val minImageSize: DataSize = DataSize.ofKilobytes(2),
        val maxImageSize: DataSize = DataSize.ofMegabytes(5),
        val siteRequestInterval: Duration = Duration.ofSeconds(1),
        val siteTimeout: Duration = Duration.ofSeconds(15),
        val imageTimeout: Duration = Duration.ofSeconds(90),
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
