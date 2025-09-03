package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.topic.TopicResponse
import com.fx.api.adapter.`in`.web.dto.topic.TopicUpdateRequest
import com.fx.api.adapter.`in`.web.swagger.TopicOpenApiSwagger
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/topics")
class TopicOpenApiAdapter(
    private val fcmTokenQueryUseCase: FcmTokenQueryUseCase,
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase
) : TopicOpenApiSwagger {

    @GetMapping
    override fun getMyTopics(@RequestHeader fcmToken: String): ResponseEntity<Api<TopicResponse>> =
        Api.OK(TopicResponse.from(fcmTokenQueryUseCase.getMyTokenInfo(fcmToken)))

    @PostMapping
    override fun updateTopics(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid topicUpdateRequest: TopicUpdateRequest
    ): ResponseEntity<Api<TopicResponse>> {
        val updatedFcmToken = fcmTokenCommandUseCase.updateTopics(topicUpdateRequest.toCommand(fcmToken))
        return Api.OK(TopicResponse.from(updatedFcmToken))
    }

}