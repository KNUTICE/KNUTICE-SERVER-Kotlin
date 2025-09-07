package com.fx.crawler.adapter.`in`.web

import com.fx.crawler.appllication.port.`in`.NotificationUseCase
import com.fx.crawler.appllication.port.`in`.dto.NoticeCommand
import com.fx.global.adapter.feign.NoticeNotificationFeignRequest
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import com.fx.global.domain.TopicType
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@WebInputAdapter
class NotificationApiAdapter(
    private val notificationUseCase: NotificationUseCase
) {

    private val log = LoggerFactory.getLogger(NotificationApiAdapter::class.java)

    @PostMapping("/open-api/v1/notification/notice")
    suspend fun pushTestNotice(
        @RequestHeader fcmToken: String,
        @RequestBody noticeNotificationFeignRequest: NoticeNotificationFeignRequest
    ): ResponseEntity<Api<Boolean>> {
        log.info("알림 발송 대상 fcmToken : {}", fcmToken)
        notificationUseCase.sendNotification(fcmToken, toCommand(noticeNotificationFeignRequest))
        return Api.OK(true)
    }

    fun toCommand(feignRequest: NoticeNotificationFeignRequest): NoticeCommand {
        val crawlableType = when (feignRequest.topicType) {
            TopicType.NOTICE -> NoticeType.valueOf(feignRequest.topic)
            TopicType.MAJOR -> MajorType.valueOf(feignRequest.topic)
            TopicType.MEAL  -> MealType.valueOf(feignRequest.topic)
        }
        return NoticeCommand(
            nttId = feignRequest.nttId,
            title = feignRequest.title,
            department = feignRequest.department,
            contentUrl = feignRequest.contentUrl,
            contentImageUrl = feignRequest.contentImageUrl,
            registrationDate = feignRequest.registrationDate,
            isAttachment = feignRequest.isAttachment,
            type = crawlableType
        )
    }

}