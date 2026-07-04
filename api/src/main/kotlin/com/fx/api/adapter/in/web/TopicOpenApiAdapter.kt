package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.topic.TopicResponse
import com.fx.api.adapter.`in`.web.dto.topic.TopicUpdateRequest
import com.fx.api.adapter.`in`.web.dto.topic.TypeResponse
import com.fx.api.adapter.`in`.web.swagger.TopicOpenApiSwagger
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.TopicType
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.exception.TopicException
import com.fx.global.exception.errorcode.TopicErrorCode
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.context.MessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@WebInputAdapter
@RequestMapping("/open-api/v1/topics")
class TopicOpenApiAdapter(
    private val fcmTokenQueryUseCase: FcmTokenQueryUseCase,
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase,
    private val messageSource: MessageSource
) : TopicOpenApiSwagger {

    @GetMapping
    override fun getMyTopics(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType
    ): ResponseEntity<Api<TopicResponse>> {
        val subscribedTopics = fcmTokenQueryUseCase.getMyTopics(fcmToken, type)
        return Api.OK(TopicResponse.from(subscribedTopics), "토픽 조회 성공")
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
        @RequestHeader(value = "Accept-Language", required = false, defaultValue = "ko-KR") acceptLanguage: String,
        @RequestParam(required = false) type: TopicType?,
        @RequestParam(required = false) topic: String?,
        @RequestParam(required = false) topicId: Int?
    ): ResponseEntity<Api<List<TypeResponse>>> {
        if (topic != null || topicId != null) {
            val resolved = topicId?.let { CrawlableType.fromCode(it) } ?: CrawlableType.fromString(topic!!)
            return Api.OK(listOf(TypeResponse.from(resolved, messageSource)))
        }

        val responses = when (type ?: throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)) {
            TopicType.NOTICE -> TypeResponse.fromNoticeTypes()
            TopicType.MAJOR -> TypeResponse.fromMajorTypes(messageSource)
            TopicType.MEAL -> TypeResponse.fromMealTypes()
        }
        return Api.OK(responses)
    }

}