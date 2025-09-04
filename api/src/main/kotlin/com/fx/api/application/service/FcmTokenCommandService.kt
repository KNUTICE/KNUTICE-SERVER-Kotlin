package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.dto.FcmTokenUpdateCommand
import com.fx.api.application.port.`in`.dto.MajorTopicUpdateCommand
import com.fx.api.application.port.`in`.dto.NoticeTopicUpdateCommand
import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.exception.FcmTokenException
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.domain.FcmToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FcmTokenCommandService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
) : FcmTokenCommandUseCase {

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

    /**
     * oldFcmToken 을 newFcmToken 으로 topic 정보 업데이트
     * oldFcmToken 이 존재하지 않는 경우 새로운 newFcmToken 생성
     * oldFcmToken 이 존재하는 경우 oldFcmToken 을 isActive = false 처리, 토픽 정보를 newFcmToken 으로 업데이트 후 저장
     */
    @Transactional
    override fun updateFcmToken(fcmTokenUpdateCommand: FcmTokenUpdateCommand): Boolean {
        fcmTokenPersistencePort.findByFcmToken(fcmTokenUpdateCommand.oldFcmToken)
            ?.let { oldFcmToken ->
                fcmTokenPersistencePort.saveFcmToken(oldFcmToken.copy(isActive = false))

                // oldFcmToken 정보를 newFcmToken 으로 복사
                fcmTokenPersistencePort.saveFcmToken(
                    FcmToken.updateFcmToken(
                        newFcmToken = fcmTokenUpdateCommand.newFcmToken,
                        oldFcmToken = oldFcmToken,
                    )
                )
            }
            ?: run {
                val newToken = FcmToken.createFcmToken(
                    fcmToken = fcmTokenUpdateCommand.newFcmToken,
                    deviceType = fcmTokenUpdateCommand.deviceType
                )
                fcmTokenPersistencePort.saveFcmToken(newToken)
            }
        return true
    }

    override fun updateNoticeTopic(noticeTopicUpdateCommand: NoticeTopicUpdateCommand): Boolean {
        val fcmToken = fcmTokenPersistencePort.findByFcmToken(noticeTopicUpdateCommand.fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

        val updatedTopics = fcmToken.subscribedTopics.toMutableSet()

        if (noticeTopicUpdateCommand.enabled) {
            updatedTopics.add(noticeTopicUpdateCommand.type.typeName)
        } else {
            updatedTopics.remove(noticeTopicUpdateCommand.type.typeName)
        }

        fcmTokenPersistencePort.saveFcmToken(fcmToken.copy(subscribedTopics = updatedTopics))
        return true
    }

    override fun updateMajorTopic(majorTopicUpdateCommand: MajorTopicUpdateCommand): Boolean {
        val fcmToken = fcmTokenPersistencePort.findByFcmToken(majorTopicUpdateCommand.fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

        val updatedTopics = fcmToken.subscribedTopics.toMutableSet()

        if (majorTopicUpdateCommand.enabled) {
            updatedTopics.add(majorTopicUpdateCommand.type.typeName)
        } else {
            updatedTopics.remove(majorTopicUpdateCommand.type.typeName)
        }

        fcmTokenPersistencePort.saveFcmToken(fcmToken.copy(subscribedTopics = updatedTopics))
        return true
    }
}