package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.NotificationUseCase
import com.fx.crawler.appllication.port.out.FcmNotificationPort
import com.fx.crawler.appllication.port.out.FcmTokenPersistencePort
import com.fx.global.domain.FcmToken
import com.fx.crawler.domain.FcmTokenQuery
import com.fx.global.domain.DeviceType
import com.fx.global.domain.Notice
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NotificationService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val fcmNotificationPort: FcmNotificationPort
): NotificationUseCase {

    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    companion object {
        const val BATCH_SIZE = 500
    }

    override suspend fun sendNotification(notices: List<Notice>) = coroutineScope {
        if (notices.isEmpty()) {
            return@coroutineScope
        }

        // 공지 타입별 그룹화
        val noticesByType: Map<String, List<Notice>> = notices.groupBy { it.type.typeName }

        noticesByType.map { (typeName, notices) ->
            log.info("TYPE : {}", typeName)
            async {
                var cursor: LocalDateTime? = null

                while (true) {
                    // TypeName batch 조회
                    val fcmTokens = fcmTokenPersistencePort.findByCreatedAtAndIsActive(
                        FcmTokenQuery(
                            createdAt = cursor,
                            isActive = true,
                            subscribedTopic = typeName,
                            pageable =  PageRequest.of(0, BATCH_SIZE, Sort.by(Sort.Direction.ASC, "createdAt"))
                        )
                    )
                    if (fcmTokens.isEmpty()) break

                    val failedTokens = fcmNotificationPort.sendNotification(fcmTokens, notices) // 타입별 전송
                    log.info("{} - 전송 실패 토큰 개수 : {} / {}", typeName, failedTokens.size, fcmTokens.size)

                    if (failedTokens.isNotEmpty()) {
                        handleFailedTokens(failedTokens) // 실패 토큰에 대해 isActive 값 변경 -> false
                    }
                    cursor = fcmTokens.last().createdAt // cursor update
                }

            }
        }.forEach { it.await() } // 비동기 병렬 전송 대기. 모든 작업이 끝나야 코루틴 종료
    }

    private fun handleFailedTokens(failedTokens: List<FcmToken>) {
        fcmTokenPersistencePort.saveAll(failedTokens.map { it.copy(isActive = false) })
    }

    override suspend fun sendSilentPushNotification() = coroutineScope {
        var cursor: LocalDateTime? = null
        val tasks = mutableListOf<Deferred<List<FcmToken>>>()
        while (true) {
            val fcmTokens = fcmTokenPersistencePort.findByCreatedAtAndIsActive(
                FcmTokenQuery(
                    createdAt = cursor,
                    isActive = true,
                    deviceType = DeviceType.iOS,
                    pageable = PageRequest.of(0, BATCH_SIZE, Sort.by(Sort.Direction.ASC, "createdAt"))
                )
            )
            if (fcmTokens.isEmpty()) break

            tasks += async {
                val failedTokens = fcmNotificationPort.sendSilentPushNotification(fcmTokens)
                log.info("SilentPush - 전송 실패 토큰 개수 : {} / {}", failedTokens.size, fcmTokens.size)
                failedTokens
            }
            cursor = fcmTokens.last().createdAt
        }
        val failedAll = tasks.awaitAll().flatten()

        if (failedAll.isNotEmpty()) {
            handleFailedTokens(failedAll) // 실패 토큰 처리
        }
    }

}