package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.domain.TopicCategory
import com.fx.api.exception.FcmTokenException
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.domain.FcmToken
import org.springframework.stereotype.Service

@Service
class FcmTokenQueryService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
): FcmTokenQueryUseCase {

    override fun getMyTopics(fcmToken: String, category: TopicCategory): FcmToken {
        val token = findTokenOrThrow(fcmToken)
        return when(category) {
            TopicCategory.NOTICE -> token.copy(subscribedNoticeTopics = token.subscribedNoticeTopics)
            TopicCategory.MAJOR -> token.copy(subscribedMajorTopics = token.subscribedMajorTopics)
            TopicCategory.MEAL -> token.copy(subscribedMealTopics = token.subscribedMealTopics)
        }
    }

    private fun findTokenOrThrow(fcmToken: String): FcmToken =
        fcmTokenPersistencePort.findByFcmToken(fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

}