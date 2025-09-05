package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.MealNotificationUseCase
import com.fx.crawler.appllication.port.out.FcmNotificationPort
import com.fx.crawler.appllication.port.out.FcmTokenPersistencePort
import com.fx.crawler.domain.FcmTokenQuery
import com.fx.global.domain.FcmToken
import com.fx.global.domain.Meal
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MealNotificationService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val fcmNotificationPort: FcmNotificationPort
) : MealNotificationUseCase {

    private val log = LoggerFactory.getLogger(MealNotificationService::class.java)

    companion object {
        const val BATCH_SIZE = 500
    }

    override suspend fun sendNotification(meals: List<Meal>) = coroutineScope {
        if (meals.isEmpty()) {
            return@coroutineScope
        }

        meals.map {
            log.info("Type : {}", it.type)
            async {
                var cursor: LocalDateTime? = null

                while (true) {
                    val query = FcmTokenQuery(
                        createdAt = cursor,
                        isActive = true,
                        subscribedMealTopic = it.type,
                        pageable = PageRequest.of(0, BATCH_SIZE, Sort.by(Sort.Direction.ASC, "createdAt"))
                    )

                    val fcmTokens = fcmTokenPersistencePort.findByCreatedAtAndIsActive(query)
                    if (fcmTokens.isEmpty()) break

                    val failedTokens = fcmNotificationPort.sendNotification(fcmTokens, it)
                    log.info("{} - 전송 실패 토큰 개수 : {} / {}", it.type, failedTokens.size, fcmTokens.size)

                    if (failedTokens.isNotEmpty()) {
                        handleFailedTokens(failedTokens) // 실패 토큰에 대해 isActive 값 변경 -> false
                    }
                    cursor = fcmTokens.last().createdAt // cursor update
                }
            }
        }.awaitAll()
    }

    private fun handleFailedTokens(failedTokens: List<FcmToken>) {
        fcmTokenPersistencePort.saveAll(failedTokens.map { it.copy(isActive = false) })
    }

}