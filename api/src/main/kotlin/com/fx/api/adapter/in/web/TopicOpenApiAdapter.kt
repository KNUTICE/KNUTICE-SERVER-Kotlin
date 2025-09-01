package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.TopicResponse
import com.fx.api.adapter.`in`.web.dto.TopicUpdateRequest
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
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
) {

    @Operation(summary = "Topic 정보 조회", description = "요청한 토큰의 토픽 정보를 확인합니다.")
    @GetMapping
    fun getMyTopics(@RequestHeader fcmToken: String): ResponseEntity<Api<TopicResponse>> =
        Api.OK(TopicResponse.from(fcmTokenQueryUseCase.getMyTokenInfo(fcmToken)))


    // TODO Header
    @PostMapping
    fun updateTopics(@RequestBody topicUpdateRequest: TopicUpdateRequest): ResponseEntity<Api<TopicResponse>> {
        val updatedFcmToken = fcmTokenCommandUseCase.updateTopics(topicUpdateRequest.from())
        return Api.OK(TopicResponse.from(updatedFcmToken))
    }

}