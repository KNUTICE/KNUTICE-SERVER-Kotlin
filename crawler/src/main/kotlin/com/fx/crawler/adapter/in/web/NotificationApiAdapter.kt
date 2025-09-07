package com.fx.crawler.adapter.`in`.web

import com.fx.crawler.appllication.port.`in`.NotificationUseCase
import com.fx.global.adapter.feign.NoticeNotificationFeignRequest
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.Notice
import com.fx.global.domain.NoticeType
import com.fx.global.domain.TopicType
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import java.time.LocalDate

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
        log.info("fcmToken : {}", fcmToken)
        log.info("notice : {}", noticeNotificationFeignRequest)
        notificationUseCase.sendNotification(fcmToken, createNotice(noticeNotificationFeignRequest))
        return Api.OK(true)
    }

    private fun createNotice(request: NoticeNotificationFeignRequest): Notice {
        val enumType = when (request.topicType) {
            TopicType.NOTICE -> NoticeType.valueOf(request.type)
            TopicType.MAJOR -> MajorType.valueOf(request.type)
            TopicType.MEAL  -> MealType.valueOf(request.type)
        }
        return Notice(
            nttId = request.nttId,
            title = request.title,
            department = request.department,
            contentUrl = request.contentUrl,
            contentImageUrl = request.contentImageUrl,
            registrationDate = request.registrationDate,
            isAttachment = request.isAttachment,
            type = enumType
        )
    }

}