package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.NotificationUseCase
import com.fx.crawler.appllication.port.`in`.dto.NoticeCommand
import com.fx.crawler.appllication.port.out.FcmNotificationPort
import com.fx.crawler.appllication.port.out.FcmTokenPersistencePort
import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.global.domain.FcmToken
import com.fx.crawler.domain.FcmTokenQuery
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.DeviceType
import com.fx.global.domain.MajorType
import com.fx.global.domain.Notice
import com.fx.global.domain.NoticeType
import com.fx.global.exception.FcmTokenException
import com.fx.global.exception.NoticeException
import com.fx.global.exception.NotificationException
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import com.fx.global.exception.errorcode.NoticeErrorCode
import com.fx.global.exception.errorcode.NotificationErrorCode
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
    private val fcmNotificationPort: FcmNotificationPort,
    private val noticePersistencePort: NoticePersistencePort
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
        val noticesByTopic: Map<CrawlableType, List<Notice>> = notices.groupBy { it.topic }

        noticesByTopic.map { (topic, notices) ->
            log.info("Topic : {}", topic)
            async {
                var cursor: LocalDateTime? = null

                while (true) {
                    // Topic 별 batch 조회
                    val baseQuery = FcmTokenQuery(
                        createdAt = cursor,
                        isActive = true,
                        pageable = PageRequest.of(0, BATCH_SIZE, Sort.by(Sort.Direction.ASC, "createdAt"))
                    )

                    val query = when (topic) {
                        is NoticeType -> baseQuery.copy(subscribedNoticeTopic = topic)
                        is MajorType -> baseQuery.copy(subscribedMajorTopic = topic)
                        else -> throw IllegalArgumentException("Unsupported topic: $topic")
                    }

                    val fcmTokens = fcmTokenPersistencePort.findByCreatedAtAndIsActive(query)
                    if (fcmTokens.isEmpty()) break

                    val failedTokens = fcmNotificationPort.sendNotification(fcmTokens, notices) // 타입별 전송
                    log.info("{} - 전송 실패 토큰 개수 : {} / {}", topic, failedTokens.size, fcmTokens.size)

                    if (failedTokens.isNotEmpty()) {
                        handleFailedTokens(failedTokens) // 실패 토큰에 대해 isActive 값 변경 -> false
                    }
                    cursor = fcmTokens.last().createdAt // cursor update
                }

            }
        }.forEach { it.await() } // 비동기 병렬 전송 대기. 모든 작업이 끝나야 코루틴 종료
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

    override suspend fun sendNotification(fcmToken: String, nttId: Long) {
        val fcmToken = fcmTokenPersistencePort.findByFcmToken(fcmToken)?: throw FcmTokenException(
            FcmTokenErrorCode.TOKEN_NOT_FOUND)
        val notices = noticePersistencePort.findByNttIdIn(listOf(nttId))
        if (notices.isEmpty()) {
            throw NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
        }

        val failedTokens = fcmNotificationPort.sendNotification(listOf(fcmToken), notices)
        if (failedTokens.isNotEmpty()) {
            log.info("{} - 전송 실패 토큰 개수 : {}", notices[0].topic, failedTokens.size)
            throw NotificationException(NotificationErrorCode.NOTIFICATION_SEND_FAILED)
        }
    }

    private fun handleFailedTokens(failedTokens: List<FcmToken>) {
        fcmTokenPersistencePort.saveAll(failedTokens.map { it.copy(isActive = false) })
    }

}