package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.topic.TopicResponse
import com.fx.api.adapter.`in`.web.dto.topic.TopicUpdateRequest
import com.fx.api.domain.TopicCategory
import com.fx.api.adapter.`in`.web.swagger.TopicOpenApiSwagger
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import com.fx.global.domain.FcmToken
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@WebInputAdapter
@RequestMapping("/open-api/v1/topics")
class TopicOpenApiAdapter(
    private val fcmTokenQueryUseCase: FcmTokenQueryUseCase,
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase
) : TopicOpenApiSwagger {

    @GetMapping
    override fun getMyTopics(
        @RequestHeader fcmToken: String,
        @RequestParam category: TopicCategory
    ): ResponseEntity<Api<TopicResponse>> {
        val myTopics = fcmTokenQueryUseCase.getMyTopics(fcmToken, category)
        return Api.OK(TopicResponse.from(myTopics, category), "토픽 조회 성공")
    }

    @PatchMapping
    override fun updateTopic(
        @RequestHeader fcmToken: String,
        @RequestParam category: TopicCategory,
        @RequestBody @Valid topicUpdateRequest: TopicUpdateRequest
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(fcmTokenCommandUseCase.updateTopic(
            topicUpdateRequest.toCommand(fcmToken, category)
        ), "토픽 업데이트 성공")

}