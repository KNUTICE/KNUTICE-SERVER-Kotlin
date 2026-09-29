package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.topic.TopicResponse
import com.fx.api.adapter.`in`.web.dto.topic.TopicUpdateRequest
import com.fx.api.adapter.`in`.web.dto.topic.TypeResponse
import com.fx.api.adapter.`in`.web.swagger.TopicOpenApiSwagger
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.api.application.port.`in`.TopicQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import com.fx.common.domain.TopicType
import com.fx.common.domain.i18n.Language
import com.fx.common.exception.TopicException
import com.fx.common.exception.errorcode.TopicErrorCode
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.Locale

@WebInputAdapter
@RequestMapping("/open-api/v1/topics")
class TopicOpenApiAdapter(
    private val fcmTokenQueryUseCase: FcmTokenQueryUseCase,
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase,
    private val topicQueryUseCase: TopicQueryUseCase,
) : TopicOpenApiSwagger {

    @GetMapping
    override fun getMyTopics(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType
    ): ResponseEntity<Api<TopicResponse>> =
        Api.OK(TopicResponse.from(fcmTokenQueryUseCase.getMyTopics(fcmToken, type)), "토픽 조회 성공")

    @PatchMapping
    override fun updateTopic(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType,
        @RequestBody @Valid topicUpdateRequest: TopicUpdateRequest
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(fcmTokenCommandUseCase.updateTopic(topicUpdateRequest.toCommand(fcmToken, type)), "토픽 업데이트 성공")

    /** topic 또는 topicId 가 있으면 그 토픽 하나를, 없으면 type 의 토픽 전체를 돌려준다. */
    @GetMapping("/types")
    override fun getTopicsByType(
        locale: Locale,
        @RequestParam(required = false) type: TopicType?,
        @RequestParam(required = false) topic: String?,
        @RequestParam(required = false) topicId: Int?
    ): ResponseEntity<Api<List<TypeResponse>>> {
        val language = Language.from(locale)

        if (topic != null || topicId != null) {
            return Api.OK(listOf(TypeResponse.from(topicQueryUseCase.getTopic(topic, topicId), language)))
        }
        val topics = topicQueryUseCase.getTopics(type ?: throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND))
        return Api.OK(TypeResponse.from(topics, language))
    }

}
