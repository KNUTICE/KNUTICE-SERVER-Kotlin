package com.fx.api.adapter.`in`.web.swagger

import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.exception.errorcode.NoticeErrorCode
import com.fx.global.exception.errorcode.NotificationErrorCode
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

@Tag(name = "알림 발송 API")
interface NotificationApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "알림 전송 실패",
                description = "알림 전송에 실패한 경우",
                value = NotificationErrorCode::class,
                constant = "NOTIFICATION_SEND_FAILED"
            ),
        ]
    )
    @Operation(summary = "알림 발송", description = "입력한 Fcm token 과 nttId 로 알림메시지를 발송합니다.")
    fun notifyNotice(
        @RequestHeader fcmToken: String,
        @PathVariable nttId: Long
    ): ResponseEntity<Api<Boolean>>

}