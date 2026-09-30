package com.fx.api.application.service.fcmtoken

import com.fx.api.application.port.`in`.fcmtoken.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenUpdateCommand
import com.fx.api.application.port.`in`.fcmtoken.dto.TopicUpdateCommand
import com.fx.api.application.port.out.fcmtoken.FcmTokenPersistencePort
import com.fx.api.application.service.topic.TopicResolver
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.TopicException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import com.fx.common.exception.errorcode.TopicErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FcmTokenCommandService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val topicResolver: TopicResolver,
) : FcmTokenCommandUseCase {

    /** 이미 있는 토큰이면 다시 활성화하고, 없으면 기본 토픽을 구독시켜 새로 만든다. */
    @Transactional
    override fun saveFcmToken(fcmTokenSaveCommand: FcmTokenSaveCommand): Boolean {
        requireValidToken(fcmTokenSaveCommand.fcmToken)

        fcmTokenPersistencePort.findByToken(fcmTokenSaveCommand.fcmToken)
            ?.activate()
            ?: fcmTokenPersistencePort.create(
                FcmToken(fcmTokenSaveCommand.fcmToken, fcmTokenSaveCommand.deviceType),
                defaultTopics(),
            )
        return true
    }

    /**
     * 앱이 새 토큰을 받았을 때 기존 토큰의 구독을 새 토큰으로 옮긴다.
     *
     * - 기존 토큰만 있으면 그 행의 토큰 값을 바꾼다 (id · 구독 · 좌석 알림이 그대로 이어진다).
     * - 둘 다 있으면 새 토큰의 구독을 기존 토큰과 같게 맞추고 기존 토큰은 비활성화한다.
     * - 새 토큰만 있으면 활성화하고, 둘 다 없으면 기본 토픽으로 새로 만든다.
     */
    @Transactional
    override fun updateFcmToken(fcmTokenUpdateCommand: FcmTokenUpdateCommand): Boolean {
        requireValidToken(fcmTokenUpdateCommand.newFcmToken)

        val oldToken = fcmTokenPersistencePort.findByToken(fcmTokenUpdateCommand.oldFcmToken)
        val newToken = fcmTokenPersistencePort.findByToken(fcmTokenUpdateCommand.newFcmToken)

        when {
            oldToken != null && newToken != null && oldToken.id != newToken.id -> {
                fcmTokenPersistencePort.copySubscriptions(requireNotNull(oldToken.id), requireNotNull(newToken.id))
                newToken.activate()
                oldToken.deactivate()
            }
            oldToken != null -> oldToken.changeToken(fcmTokenUpdateCommand.newFcmToken)
            newToken != null -> newToken.activate()
            else -> fcmTokenPersistencePort.create(
                FcmToken(fcmTokenUpdateCommand.newFcmToken, fcmTokenUpdateCommand.deviceType),
                defaultTopics(),
            )
        }
        return true
    }

    @Transactional
    override fun updateTopic(topicUpdateCommand: TopicUpdateCommand): Boolean {
        val fcmToken = fcmTokenPersistencePort.getByToken(topicUpdateCommand.fcmToken)
        val topic = topicResolver.byNameOrCode(
            topicName = topicUpdateCommand.topicName,
            topicId = topicUpdateCommand.topicId,
            expectedTypeForName = topicUpdateCommand.topicType,
        ) ?: throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)

        val fcmTokenId = requireNotNull(fcmToken.id)
        if (topicUpdateCommand.enabled) {
            fcmTokenPersistencePort.subscribe(fcmTokenId, topic)
        } else {
            fcmTokenPersistencePort.unsubscribe(fcmTokenId, topic.code)
        }
        return true
    }

    /** 새 토큰은 앱에 노출된 공지 · 학식 토픽을 모두 구독한 상태로 시작한다. 학과는 사용자가 고른다. */
    private fun defaultTopics(): List<TopicView> =
        catalogQueryUseCase.getTopicCatalog().topics.filter {
            it.visible && (it.topicType == TopicType.NOTICE || it.topicType == TopicType.MEAL)
        }

    private fun requireValidToken(token: String) {
        if (token.isBlank()) {
            throw FcmTokenException(FcmTokenErrorCode.TOKEN_INVALID)
        }
    }

}
