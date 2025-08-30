package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.NotificationUseCase
import com.fx.crawler.appllication.port.out.FcmNotificationPort
import com.fx.crawler.appllication.port.out.FcmTokenPersistencePort
import com.fx.crawler.domain.FcmToken
import com.fx.crawler.domain.FcmTokenQuery
import com.fx.crawler.domain.Notice
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
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
        log.info("notice group type: {}", noticesByType)

        noticesByType.map { (typeName, noticeList) ->
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
                            pageable = Pageable.ofSize(BATCH_SIZE)
                        )
                    )
                    if (fcmTokens.isEmpty()) break

                    val message = buildMessage(noticeList)
                    val failedTokens = fcmNotificationPort.sendNotification(fcmTokens, noticeList[0].type, message)
                    log.info("{} - 전송 실패 토큰 개수 : {} / {}", typeName, failedTokens.size, BATCH_SIZE)

                    handleFailedTokens(failedTokens) // 실패 토큰에 대해 isActive 값 변경 -> false
                    cursor = fcmTokens.last().createdAt // cursor update
                }

            }
        }.forEach { it.await() } // 비동기 병렬 전송 대기. 모든 작업이 끝나야 코루틴 종료
    }

    private fun buildMessage(noticeList: List<Notice>): String {
        return if (noticeList.size == 1) {
            noticeList[0].title
        } else {
            "${noticeList[0].title} 외 ${noticeList.size - 1}개의 소식이 있습니다."
        }
    }

    private fun handleFailedTokens(failedTokens: List<FcmToken>) {
        fcmTokenPersistencePort.saveAll(failedTokens.map { it.copy(isActive = false) })
    }


}