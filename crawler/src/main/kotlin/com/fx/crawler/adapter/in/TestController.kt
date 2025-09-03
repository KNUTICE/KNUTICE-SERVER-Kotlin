package com.fx.crawler.adapter.`in`

import com.fx.crawler.adapter.out.persistence.repository.FcmTokenQueryRepository
import com.fx.crawler.domain.FcmTokenQuery
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
class TestController(
    private val fcmTokenQueryRepository: FcmTokenQueryRepository
) {

    private val log = LoggerFactory.getLogger(TestController::class.java)

    @GetMapping("/fcm")
    fun getFcmTokens() {

        val BATCH_SIZE = 100

        var cursor = OffsetDateTime
            .parse("2024-07-05T23:34:37.369+00:00")
            .toLocalDateTime()

        while (true) {
            val fcmTokens = fcmTokenQueryRepository.findByCreatedAtAndIsActive(
                FcmTokenQuery(
                    createdAt = cursor,
                    isActive = false,
                    pageable = PageRequest.of(0, BATCH_SIZE, Sort.by(Sort.Direction.ASC, "createdAt"))
                )
            )

            if (fcmTokens.isEmpty()) {
                break
            }
            log.info("fcmToken size : {}", fcmTokens.size)
            log.info("last : {}", fcmTokens.last().createdAt)
            cursor = fcmTokens.last().createdAt;
        }
    }


}