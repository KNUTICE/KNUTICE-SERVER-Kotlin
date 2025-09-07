package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.notification.NoticeNotificationRequest
import com.fx.api.application.port.`in`.NotificationUseCase
import com.fx.global.domain.TopicType
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@WebInputAdapter
@RequestMapping("/open-api/v1/notification")
class NotificationApiAdapter(
    private val notificationUseCase: NotificationUseCase
) {

    private val log = LoggerFactory.getLogger(NoticeOpenApiAdapter::class.java)

    @PostMapping("/notices")
    fun pushTestNotice(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType,
        @RequestBody notificationRequest: NoticeNotificationRequest
    ): ResponseEntity<Api<Boolean>> {
        return Api.OK(notificationUseCase.sendTestNotice(notificationRequest.toCommand(fcmToken, type)))
    }

}