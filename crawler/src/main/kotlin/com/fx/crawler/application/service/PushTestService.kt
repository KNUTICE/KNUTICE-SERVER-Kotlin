package com.fx.crawler.application.service

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.TopicType
import com.fx.common.exception.NotificationException
import com.fx.common.exception.TopicException
import com.fx.common.exception.errorcode.NotificationErrorCode
import com.fx.common.exception.errorcode.TopicErrorCode
import com.fx.crawler.application.port.`in`.PushSendUseCase
import com.fx.crawler.application.port.`in`.PushTestUseCase
import com.fx.crawler.application.port.out.FcmTokenPersistencePort
import com.fx.crawler.application.port.out.MealPort
import com.fx.crawler.application.port.out.NoticePersistencePort
import com.fx.crawler.domain.push.LocalizedPushMessages
import com.fx.crawler.domain.push.MealPushComposer
import com.fx.crawler.domain.push.NoticePushComposer
import com.fx.crawler.domain.push.PushTarget
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate

@Service
class PushTestService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val noticePersistencePort: NoticePersistencePort,
    private val mealPort: MealPort,
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val pushSendUseCase: PushSendUseCase,
    private val clock: Clock,
) : PushTestUseCase {

    override fun sendNotice(fcmToken: String, nttId: Long) {
        val target = fcmTokenPersistencePort.getTargetByToken(fcmToken)
        val notice = noticePersistencePort.getByNttId(nttId)
        val topic = catalogQueryUseCase.getTopicCatalog().findByCode(notice.topicCode)
            ?: throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)

        send(target, NoticePushComposer.compose(topic, listOf(notice), catalogQueryUseCase.getNotificationTemplateCatalog()))
    }

    /** 오늘 식단이 없으면 보낼 알림이 없으므로 발송 실패로 응답한다. */
    override fun sendMeal(fcmToken: String, mealTopicName: String) {
        val topic = catalogQueryUseCase.getTopicCatalog().findByName(mealTopicName)
            ?.takeIf { it.topicType == TopicType.MEAL }
            ?: throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)
        val target = fcmTokenPersistencePort.getTargetByToken(fcmToken)
        val meal = mealPort.fetchMeal(topic, LocalDate.now(clock))
            ?: throw NotificationException(NotificationErrorCode.NOTIFICATION_SEND_FAILED)

        send(target, MealPushComposer.compose(topic, meal, catalogQueryUseCase.getNotificationTemplateCatalog()))
    }

    private fun send(target: PushTarget, messages: LocalizedPushMessages) {
        if (pushSendUseCase.send(listOf(target), messages).hasInvalidToken) {
            throw NotificationException(NotificationErrorCode.NOTIFICATION_SEND_FAILED)
        }
    }

}
