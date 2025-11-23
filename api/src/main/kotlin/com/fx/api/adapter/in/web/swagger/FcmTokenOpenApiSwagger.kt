package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.fcm.FcmTokenSaveRequest
import com.fx.api.adapter.`in`.web.dto.fcm.FcmTokenUpdateRequest
import com.fx.api.exception.errorcode.ImageErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Tag(name = "토큰 관리 API")
interface FcmTokenOpenApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "토큰 저장 실패",
                description = "토큰 형식이 올바르지 않은 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_INVALID"
            ),
        ]
    )
    @Operation(summary = "토큰 저장", description = "토큰이 없는 경우 새로 저장하며 있는 경우 isActive 값을 true 로 활성화하여 저장합니다.")
    fun saveFcmToken(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid tokenSaveRequest: FcmTokenSaveRequest
    ): ResponseEntity<Api<Boolean>>

    @Operation(summary = "새로운 토큰으로 업데이트", description = "Silent Push 요청 시 사용되는 API 입니다.<br>" +
            "header 에는 새로운 fcmToken 값을 넣으며, Body 에는 oldFcmToken, deviceType 을 지정합니다.")
    fun updateFcmToken(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid tokenUpdateRequest: FcmTokenUpdateRequest
    ): ResponseEntity<Api<Boolean>>

}