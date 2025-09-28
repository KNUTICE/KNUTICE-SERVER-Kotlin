package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.notification.NoticeNotificationRequest
import com.fx.global.domain.TopicType
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "알림 발송 API")
interface NotificationApiSwagger {

    @Operation(summary = "알림 발송", description = "입력한 Fcm token 으로 알림메시지를 발송합니다.")
    fun pushTestNotice(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType,
        @RequestBody notificationRequest: NoticeNotificationRequest
    ): ResponseEntity<Api<Boolean>>

}