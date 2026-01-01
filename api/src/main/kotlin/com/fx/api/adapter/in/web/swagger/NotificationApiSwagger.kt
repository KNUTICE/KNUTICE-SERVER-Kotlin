package com.fx.api.adapter.`in`.web.swagger

import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.domain.MealType
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import com.fx.global.exception.errorcode.NoticeErrorCode
import com.fx.global.exception.errorcode.NotificationErrorCode
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
            ApiExceptionExplanation(
                name = "Topic 조회 실패",
                description = "Fcm token 이 존재하지 않는 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "공지 알림 발송", description = "입력한 Fcm token 과 nttId 로 알림메시지를 발송합니다.")
    fun notifyNotice(
        @RequestHeader fcmToken: String,
        @PathVariable nttId: Long
    ): ResponseEntity<Api<Boolean>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "알림 전송 실패",
                description = "알림 전송에 실패한 경우",
                value = NotificationErrorCode::class,
                constant = "NOTIFICATION_SEND_FAILED"
            ),
            ApiExceptionExplanation(
                name = "Topic 조회 실패",
                description = "Fcm token 이 존재하지 않는 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "학식 알림 발송", description = "입력한 Fcm token 과 mealType 으로 알림메시지를 발송합니다. <br>" +
            "mealType : `STUDENT_CAFETERIA(학생식당)`, `STAFF_CAFETERIA(교직원식당)` <br>" +
            "**학식 정보가 없는 경우 500 에러가 발생합니다.**")
    fun notifyMeal(
        @RequestHeader fcmToken: String,
        @PathVariable mealType: MealType
    ):  ResponseEntity<Api<Boolean>>

}