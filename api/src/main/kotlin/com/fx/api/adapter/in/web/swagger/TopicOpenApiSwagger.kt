package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.topic.MajorTopicUpdateRequest
import com.fx.api.adapter.`in`.web.dto.topic.NoticeTopicUpdateRequest
import com.fx.api.adapter.`in`.web.dto.topic.TopicResponse
import com.fx.api.adapter.`in`.web.dto.topic.TopicUpdateRequest
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
    @Operation(summary = "Notice Topic 조회", description = "기본 Topic 을 조회합니다.")
    fun getMyNoticeTopics(@RequestHeader fcmToken: String): ResponseEntity<Api<TopicResponse>>

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
    @Operation(summary = "Major Topic 조회", description = "학과 Topic 을 조회합니다.")
    fun getMyMajorTopics(@RequestHeader fcmToken: String): ResponseEntity<Api<TopicResponse>>


    @Operation(summary = "Notice Topic 변경", description = "Notice Topic 하나를 변경합니다.")
    fun updateNoticeTopic(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid noticeTopicUpdateRequest: NoticeTopicUpdateRequest
    ): ResponseEntity<Api<Boolean>>

    @Operation(summary = "Major Topic 변경", description = "Major Topic 하나를 변경합니다.")
    fun updateMajorTopic(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid majorTopicUpdateRequest: MajorTopicUpdateRequest
    ): ResponseEntity<Api<Boolean>>

}