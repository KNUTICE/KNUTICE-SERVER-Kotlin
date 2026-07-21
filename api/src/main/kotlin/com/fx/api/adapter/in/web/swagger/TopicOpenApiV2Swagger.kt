package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.topic.TopicResponseV2
import com.fx.api.adapter.`in`.web.dto.topic.TopicUpdateRequestV2
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.domain.TopicType
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import com.fx.global.exception.errorcode.TopicErrorCode
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "TOPIC 관리 API")
interface TopicOpenApiV2Swagger {

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
    @Operation(
        summary = "Topic 조회 V2",
        description = "Topic 을 조회합니다.<br> type 는 NOTICE, MAJOR, MEAL 입니다. <br> [2026.01.21] 해당 API 는 Integer topicIds 를 반환합니다. <br>" +
                "[2026.07.21] 응답에 subscribedTopics(topic·topicId·name·college 객체 리스트)가 추가되었습니다. " +
                "문자열로 저장된 기존 구독 정보를 신규 학과 공지 관리 객체로 변환해 반환하며, 클라이언트 마이그레이션 용도입니다."
    )
    fun getMyTopics(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType
    ): ResponseEntity<Api<TopicResponseV2>>

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
    @Operation(
        summary = "Topic 변경 V2",
        description = "Notice, Major, Meal Type 에 존재하는 topic 을 변경합니다. <br> [2026.01.21] 해당 API 는 Integer topicIds 를 받습니다."
    )
    fun updateTopic(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType,
        @RequestBody @Valid topicUpdateRequestV2: TopicUpdateRequestV2
    ): ResponseEntity<Api<Boolean>>

}