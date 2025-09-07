package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.topic.TypeResponse
import com.fx.api.adapter.`in`.web.dto.topic.TopicResponse
import com.fx.api.adapter.`in`.web.dto.topic.TopicUpdateRequest
import com.fx.global.domain.TopicType
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.api.exception.errorcode.TopicErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

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
    @Operation(summary = "Topic 조회", description = "Topic 을 조회합니다.<br> type 는 NOTICE, MAJOR, MEAL 입니다.")
    fun getMyTopics(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType
    ): ResponseEntity<Api<TopicResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "유효하지 않은 Topic",
                description = "서버에 존재하지 않은 Topic 이름인 경우",
                value = TopicErrorCode::class,
                constant = "TOPIC_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "Topic 변경", description = "Notice, Major, Meal Type 에 존재하는 topic 을 변경합니다.")
    fun updateTopic(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType,
        @RequestBody @Valid topicUpdateRequest: TopicUpdateRequest
    ): ResponseEntity<Api<Boolean>>

    @Operation(summary = "Type 별 Topic 조회", description = "각 타입의 토픽들을 조회합니다.")
    fun getTopicsByType(
        @RequestParam type: TopicType
    ): ResponseEntity<Api<List<TypeResponse>>>

}