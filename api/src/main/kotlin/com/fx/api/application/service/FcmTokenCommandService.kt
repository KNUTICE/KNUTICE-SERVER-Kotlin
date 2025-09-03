package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.exception.FcmTokenException
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.domain.FcmToken
import org.springframework.stereotype.Service

@Service
class FcmTokenCommandService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
): FcmTokenCommandUseCase {

    override fun saveFcmToken(fcmTokenSaveCommand: FcmTokenSaveCommand): Boolean {
        val fcmToken = fcmTokenPersistencePort.findByFcmToken(fcmTokenSaveCommand.fcmToken)
            ?.copy(isActive = true) // 존재하는 경우 isActive = true
            ?: FcmToken.createFcmToken( // 없으면 새로 생성
                fcmToken = fcmTokenSaveCommand.fcmToken,
                deviceType = fcmTokenSaveCommand.deviceType
            )

        fcmTokenPersistencePort.saveFcmToken(fcmToken)
        return true
    }

    override fun updateTopics(topicUpdateCommand: TopicUpdateCommand): FcmToken {
        val fcmToken = fcmTokenPersistencePort.findByFcmToken(topicUpdateCommand.fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

        val updatedToken = fcmToken.copy(subscribedTopics = topicUpdateCommand.subscribedTopics)
        fcmTokenPersistencePort.saveFcmToken(updatedToken)
        return updatedToken
    }

}