package com.fx.api.application.service

import com.fx.api.application.port.`in`.NotificationUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.application.port.out.NotificationWebPort
import com.fx.common.domain.TopicType
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.NoticeException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import com.fx.common.exception.errorcode.NoticeErrorCode
import org.springframework.stereotype.Service

/**
 * 관리자 테스트 발송. 실제 발송은 crawler 가 한다.
 * crawler 호출을 기다리는 동안 DB 커넥션을 잡지 않도록 트랜잭션을 걸지 않는다.
 */
@Service
class NotificationService(
    private val notificationWebPort: NotificationWebPort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val noticePersistencePort: NoticePersistencePort,
    private val topicResolver: TopicResolver,
) : NotificationUseCase {

    override fun notifyNotice(fcmToken: String, nttId: Long): Boolean {
        requireExistingToken(fcmToken)
        if (!noticePersistencePort.existsByNttId(nttId)) {
            throw NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
        }
        return notificationWebPort.notifyNotice(fcmToken, nttId)
    }

    override fun notifyMeal(fcmToken: String, mealTopicName: String): Boolean {
        requireExistingToken(fcmToken)
        val mealTopic = topicResolver.byName(mealTopicName, TopicType.MEAL)
        return notificationWebPort.notifyMeal(fcmToken, mealTopic.name)
    }

    private fun requireExistingToken(fcmToken: String) {
        if (!fcmTokenPersistencePort.existsByToken(fcmToken)) {
            throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)
        }
    }

}
