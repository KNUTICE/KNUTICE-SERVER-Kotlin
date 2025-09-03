package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.TopicResponse
import com.fx.api.adapter.`in`.web.dto.TopicUpdateRequest
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Tag(name = "TOPIC 관리 API")
interface TopicOpenApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "Topic 조회 실패",
                description = "Fcm token 이 존재하지 않는 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "Topic 정보 조회", description = "요청한 토큰의 Topic 정보를 확인합니다.")
    fun getMyTopics(@RequestHeader fcmToken: String): ResponseEntity<Api<TopicResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "Topic 조회 실패",
                description = "Fcm token 이 존재하지 않는 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "Topic 변경", description = "변경된 Topic 을 업데이트합니다.")
    fun updateTopics(
        @RequestHeader fcmToken: String,
        @RequestBody topicUpdateRequest: TopicUpdateRequest
    ): ResponseEntity<Api<TopicResponse>>
}