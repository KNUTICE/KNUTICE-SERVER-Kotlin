package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.global.domain.TopicType
import com.fx.global.exception.FcmTokenException
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import com.fx.global.domain.FcmToken
import org.springframework.stereotype.Service

@Service
class FcmTokenQueryService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
): FcmTokenQueryUseCase {

    override fun getMyTopics(fcmToken: String, type: TopicType): Set<String> {
        val token = findTokenOrThrow(fcmToken)
        val subscribedTopics = when (type) {
            TopicType.NOTICE -> token.subscribedNoticeTopics
            TopicType.MAJOR -> token.subscribedMajorTopics
            TopicType.MEAL -> token.subscribedMealTopics
        }
        return subscribedTopics.map { it.name }.toSet()
    }

    private fun findTokenOrThrow(fcmToken: String): FcmToken =
        fcmTokenPersistencePort.findByFcmToken(fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

}