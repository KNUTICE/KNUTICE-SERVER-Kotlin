package com.fx.api.adapter.`in`.web.fcmtoken

import com.fx.api.adapter.`in`.web.fcmtoken.dto.FcmTokenLanguageResponse
import com.fx.api.adapter.`in`.web.fcmtoken.dto.FcmTokenLanguageUpdateRequest
import com.fx.api.adapter.`in`.web.fcmtoken.dto.FcmTokenSaveRequest
import com.fx.api.adapter.`in`.web.fcmtoken.dto.FcmTokenUpdateRequest
import com.fx.api.exception.errorcode.ImageErrorCode
import com.fx.common.annotation.ApiExceptionExplanation
import com.fx.common.annotation.ApiResponseExplanations
import com.fx.common.exception.errorcode.FcmTokenErrorCode
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

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "언어 조회 실패",
                description = "저장되지 않은 토큰인 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "알림 언어 조회", description = "토큰이 알림을 받는 언어(ko · en · ja)를 반환합니다.<br>" +
            "언어를 바꾼 적이 없는 토큰은 ko 입니다.")
    fun getLanguage(
        @RequestHeader fcmToken: String
    ): ResponseEntity<Api<FcmTokenLanguageResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "언어 변경 실패 - 토큰",
                description = "저장되지 않은 토큰인 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
            ApiExceptionExplanation(
                name = "언어 변경 실패 - 언어",
                description = "ko · en · ja 가 아닌 언어인 경우",
                value = FcmTokenErrorCode::class,
                constant = "LANGUAGE_NOT_SUPPORTED"
            ),
        ]
    )
    @Operation(summary = "알림 언어 변경", description = "앱에서 언어를 바꿨을 때 호출합니다. 이후 알림 제목 · 문구를 이 언어로 보냅니다.<br>" +
            "header 에는 fcmToken, Body 에는 language(ko · en · ja)를 지정합니다. 대소문자 · 지역 코드는 무시합니다 (ja-JP, ja_JP → ja).<br>" +
            "공지 제목 · 식단 메뉴 같은 학교 원문은 번역하지 않습니다.")
    fun updateLanguage(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid languageUpdateRequest: FcmTokenLanguageUpdateRequest
    ): ResponseEntity<Api<Boolean>>

}