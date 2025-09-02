package com.fx.crawler.adapter.`in`

import com.fx.crawler.adapter.out.persistence.repository.FcmTokenQueryRepository
import com.fx.crawler.adapter.out.crawler.NoticeCrawlAdapter
import com.fx.crawler.adapter.out.persistence.FcmTokenPersistenceAdapter
import com.fx.crawler.appllication.service.NoticeCrawlService
import com.fx.global.domain.DeviceType
import com.fx.global.domain.FcmToken
import com.fx.crawler.domain.FcmTokenQuery
import com.fx.global.domain.NoticeType
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random
import kotlin.time.measureTimedValue

@RestController
class TestController(
    private val noticeCrawlAdapter: NoticeCrawlAdapter,
    private val noticeCrawlService: NoticeCrawlService,
    private val fcmTokenPersistenceAdapter: FcmTokenPersistenceAdapter,
    private val fcmTokenQueryRepository: FcmTokenQueryRepository
) {

    @GetMapping("/crawl/{pageSize}")
    suspend fun crawlPage(@PathVariable pageSize: Int) {
        noticeCrawlService.crawlAndSaveNotices(NoticeType.entries, pageSize)
    }

    @GetMapping("/test")
    fun test() = runBlocking {
        val result = measureTimedValue {
            noticeCrawlService.crawlAndSaveNotices(NoticeType.values().toList())

        }
        println("new notices : ${result.value.size}")
        println("실행 시간: ${result.duration.inWholeMilliseconds} ms")
    }

    @GetMapping("/save-token")
    fun saveToken() {
        val allTopics = NoticeType.entries.map { it.name }.toSet()

        fcmTokenPersistenceAdapter.save(FcmToken(
            fcmToken = "ffffasdfasdfasdfasdfasdfasdfa",
            subscribedTopics = allTopics,
            deviceType = DeviceType.AOS,
            isActive = true,
            createdAt = LocalDateTime.now(),
        ))

        fcmTokenPersistenceAdapter.save(FcmToken(
            fcmToken = "ffffasdfasdfasdfasdfasdfasdfaffffasdfasdfasdfasdfasdfasdfaffffasdfasdfasdfasdfasdfasdfaffffasdfasdfasdfasdfasdfasdfaasdf",
            subscribedTopics = allTopics,
            deviceType = DeviceType.AOS,
            isActive = true,
            createdAt = LocalDateTime.now(),
        ))

        for (i in 1..500) {
            val fcmToken = FcmToken(
                fcmToken = UUID.randomUUID().toString(),
                subscribedTopics = allTopics, // 최초 등록 시 모든 토픽 구독
                deviceType = DeviceType.AOS,
                isActive = true,
                createdAt = randomDateTimeWithinLastYear()
            )
            fcmTokenPersistenceAdapter.save(fcmToken)
        }

    }

    @GetMapping("/delete-topic")
    fun deleteTopic() {
        val fcmToken = fcmTokenPersistenceAdapter.findByToken("abcde") ?: throw RuntimeException("ttttt")

        val topicToRemove = NoticeType.GENERAL_NEWS.name

        val updatedTopics = fcmToken.subscribedTopics.toMutableSet().apply {
            remove(topicToRemove)
        }

        // 4. 새로운 FcmToken 객체 생성
        val updatedFcmToken = fcmToken.copy(subscribedTopics = updatedTopics)

        // 5. 저장
        fcmTokenPersistenceAdapter.save(updatedFcmToken)

    }



    @GetMapping("/show")
    fun get() {
        // 기준 날짜
        val createdAt = OffsetDateTime
            .parse("2024-10-05T23:34:37.369+00:00")
            .toLocalDateTime()

        // 페이지네이션 + 정렬
        val pageable: Pageable = PageRequest.of(
            0, // 첫 페이지
            10, // 페이지 사이즈
            Sort.by(Sort.Direction.DESC, "createdAt") // createdAt 기준 내림차순
        )

        // Repository 조회
        val results = fcmTokenQueryRepository.findByCreatedAtAndIsActive(
            FcmTokenQuery(
                createdAt = createdAt,
                isActive = true,
                pageable = pageable
            )
        )

        println("조회 기준 날짜 : $createdAt")
        println("조회된 토큰 개수: ${results.size}")
        results.forEach {
            val domain = it.toDomain()
            println("RESULT : ${domain.fcmToken} - ${domain.createdAt} - ${domain.isActive}")
        }
    }

    @GetMapping("/batch")
    fun processFcmTokens() {
        var cursor: LocalDateTime? = null
        val batchSize = 500

        while (true) {
            val pageable = PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, "createdAt"))
            val batch = fcmTokenQueryRepository.findByCreatedAtAndIsActive(
                FcmTokenQuery(
                    createdAt = cursor,
                    isActive = true,
                    subscribedTopic = "EVENT_NEWS",
                    pageable = pageable
                )
            )
            println("size : ${batch.size}")
            if (batch.isEmpty()) break

            batch.forEach { token -> println("${token.toDomain().fcmToken} - ${token.toDomain().createdAt} - ${token.toDomain().isActive}" )  }
            println("----------------------------------------------------------------------------------------")
            cursor = batch.last().toDomain().createdAt
        }
    }

    fun randomDateTimeWithinLastYear(): LocalDateTime {
        val now = LocalDateTime.now()
        val oneYearAgo = now.minusYears(1)

        // 1년 전 ~ 지금까지의 총 초(second) 차이
        val seconds = ChronoUnit.SECONDS.between(oneYearAgo, now)

        // 0 ~ seconds 범위의 랜덤 오프셋
        val randomSeconds = Random.nextLong(seconds)

        return oneYearAgo.plusSeconds(randomSeconds)
    }


}