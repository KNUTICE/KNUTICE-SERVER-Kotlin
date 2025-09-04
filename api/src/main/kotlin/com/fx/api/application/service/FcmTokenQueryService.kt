package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.exception.FcmTokenException
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.FcmToken
import com.fx.global.domain.MajorType
import com.fx.global.domain.NoticeType
import org.springframework.stereotype.Service

@Service
class FcmTokenQueryService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
): FcmTokenQueryUseCase {

    override fun getMyNoticeTopics(fcmToken: String): FcmToken {
        val token = findTokenOrThrow(fcmToken)
        val noticeTopics = extractTopics<NoticeType>(token)
        return token.copy(subscribedTopics = noticeTopics)
    }

    override fun getMyMajorTopics(fcmToken: String): FcmToken {
        val token = findTokenOrThrow(fcmToken)
        val majorTopics = extractTopics<MajorType>(token)
        return token.copy(subscribedTopics = majorTopics)
    }

    private fun findTokenOrThrow(fcmToken: String): FcmToken =
        fcmTokenPersistencePort.findByFcmToken(fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

    private inline fun <reified T : CrawlableType> extractTopics(fcmToken: FcmToken): Set<String> =
        fcmToken.subscribedTopics
            .mapNotNull { runCatching { CrawlableType.fromString(it) }.getOrNull() }
            .filterIsInstance<T>()
            .map { it.typeName }
            .toSet()

}