package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.topic.MajorTopicUpdateRequest
import com.fx.api.adapter.`in`.web.dto.topic.TopicResponse
import com.fx.api.adapter.`in`.web.dto.topic.MealTopicUpdateRequest
import com.fx.api.adapter.`in`.web.dto.topic.NoticeTopicUpdateRequest
import com.fx.api.domain.TopicCategory
import com.fx.api.adapter.`in`.web.swagger.TopicOpenApiSwagger
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import com.fx.global.domain.FcmToken
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
        return Api.OK(TopicResponse.from(myTopics, category))
    }

    // TODO 예외핸들링
    @PatchMapping("/notice")
    override fun updateNoticeTopic(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid noticeTopicUpdateRequest: NoticeTopicUpdateRequest
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(fcmTokenCommandUseCase.updateNoticeTopic(noticeTopicUpdateRequest.toCommand(fcmToken)))

    @PatchMapping("/major")
    override fun updateMajorTopic(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid majorTopicUpdateRequest: MajorTopicUpdateRequest
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(fcmTokenCommandUseCase.updateMajorTopic(majorTopicUpdateRequest.toCommand(fcmToken)))

    @PatchMapping("/meal")
    override fun updateMealTopic(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid mealTopicUpdateRequest: MealTopicUpdateRequest
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(fcmTokenCommandUseCase.updateMealTopic(mealTopicUpdateRequest.toCommand(fcmToken)))


}