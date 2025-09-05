package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.topic.TopicResponse
import com.fx.api.adapter.`in`.web.dto.topic.TopicUpdateRequest
import com.fx.api.adapter.`in`.web.dto.topic.TypeResponse
import com.fx.api.adapter.`in`.web.swagger.TopicOpenApiSwagger
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.api.domain.TopicType
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@WebInputAdapter
@RequestMapping("/open-api/v1/topics")
class TopicOpenApiAdapter(
    private val fcmTokenQueryUseCase: FcmTokenQueryUseCase,
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase
) : TopicOpenApiSwagger {

    @GetMapping
    override fun getMyTopics(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType
    ): ResponseEntity<Api<TopicResponse>> {
        val myTopics = fcmTokenQueryUseCase.getMyTopics(fcmToken, type)
        return Api.OK(TopicResponse.from(myTopics, type), "토픽 조회 성공")
    }

    @PatchMapping
    override fun updateTopic(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType,
        @RequestBody @Valid topicUpdateRequest: TopicUpdateRequest
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(fcmTokenCommandUseCase.updateTopic(
            topicUpdateRequest.toCommand(fcmToken, type)
        ), "토픽 업데이트 성공")

    @GetMapping("/types")
    override fun getTopicsByType(
        @RequestParam type: TopicType
    ): ResponseEntity<Api<List<TypeResponse>>> {
        val responses = when (type) {
            TopicType.NOTICE -> TypeResponse.fromNoticeTypes()
            TopicType.MAJOR -> TypeResponse.fromMajorTypes()
            TopicType.MEAL -> TypeResponse.fromMealTypes()
        }
        return Api.OK(responses)
    }

}