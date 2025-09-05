package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.dto.FcmTokenUpdateCommand
import com.fx.api.application.port.`in`.dto.topic.MajorTopicUpdateCommand
import com.fx.api.application.port.`in`.dto.topic.MealTopicUpdateCommand
import com.fx.api.application.port.`in`.dto.topic.NoticeTopicUpdateCommand
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.exception.FcmTokenException
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.domain.FcmToken
import com.fx.global.domain.MealType
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
        val fcmToken = findTokenOrThrow(noticeTopicUpdateCommand.fcmToken)
        val updatedTopics = updateTopicSet(
            fcmToken.subscribedNoticeTopics,
            noticeTopicUpdateCommand.type,
            noticeTopicUpdateCommand.enabled
        )
        fcmTokenPersistencePort.saveFcmToken(fcmToken.copy(subscribedNoticeTopics = updatedTopics))
        return true
    }

    override fun updateMajorTopic(majorTopicUpdateCommand: MajorTopicUpdateCommand): Boolean {
        val fcmToken = findTokenOrThrow(majorTopicUpdateCommand.fcmToken)
        val updatedTopics = updateTopicSet(
            fcmToken.subscribedMajorTopics,
            majorTopicUpdateCommand.type,
            majorTopicUpdateCommand.enabled
        )
        fcmTokenPersistencePort.saveFcmToken(fcmToken.copy(subscribedMajorTopics = updatedTopics))
        return true
    }

    override fun updateMealTopic(mealTopicUpdateCommand: MealTopicUpdateCommand): Boolean {
        val fcmToken = findTokenOrThrow(mealTopicUpdateCommand.fcmToken)
        val updatedTopics = updateTopicSet(
            fcmToken.subscribedMealTopics,
            mealTopicUpdateCommand.type,
            mealTopicUpdateCommand.enabled
        )
        fcmTokenPersistencePort.saveFcmToken(fcmToken.copy(subscribedMealTopics = updatedTopics))
        return true
    }

    private fun <T> updateTopicSet(
        currentTopics: Set<T>,
        topic: T, enabled: Boolean
    ): Set<T> {
        val updated = currentTopics.toMutableSet()
        if (enabled) {
            updated.add(topic)
        } else {
            updated.remove(topic)
        }
        return updated
    }

    private fun findTokenOrThrow(fcmToken: String): FcmToken =
        fcmTokenPersistencePort.findByFcmToken(fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)


}