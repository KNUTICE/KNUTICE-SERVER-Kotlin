package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.exception.FcmTokenException
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.domain.FcmToken
import org.springframework.stereotype.Service

@Service
class FcmTokenQueryService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
): FcmTokenQueryUseCase {

    override fun getMyNoticeTopics(fcmToken: String): FcmToken {
        val token = findTokenOrThrow(fcmToken)
        return token.copy(subscribedNoticeTopics = token.subscribedNoticeTopics)
    }

    override fun getMyMajorTopics(fcmToken: String): FcmToken {
        val token = findTokenOrThrow(fcmToken)
        return token.copy(subscribedMajorTopics = token.subscribedMajorTopics)
    }

    private fun findTokenOrThrow(fcmToken: String): FcmToken =
        fcmTokenPersistencePort.findByFcmToken(fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

    override fun getMyMealTopics(fcmToken: String): FcmToken =
        fcmTokenPersistencePort.findByFcmToken(fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)
}