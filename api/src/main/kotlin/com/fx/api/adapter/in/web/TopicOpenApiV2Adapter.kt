package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.topic.TopicResponseV2
import com.fx.api.adapter.`in`.web.dto.topic.TopicUpdateRequestV2
import com.fx.api.adapter.`in`.web.swagger.TopicOpenApiV2Swagger
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.domain.TopicType
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.context.MessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@WebInputAdapter
@RequestMapping("/open-api/v2/topics")
class TopicOpenApiV2Adapter(
    private val fcmTokenQueryUseCase: FcmTokenQueryUseCase,
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase,
    private val messageSource: MessageSource
) : TopicOpenApiV2Swagger {

    @GetMapping
    override fun getMyTopics(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType
    ): ResponseEntity<Api<TopicResponseV2>> =
        Api.OK(
            TopicResponseV2.from(fcmTokenQueryUseCase.getMyTopics(fcmToken, type), messageSource),
            "토픽 조회 성공"
        )


    @PatchMapping
    override fun updateTopic(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType,
        @RequestBody @Valid topicUpdateRequestV2: TopicUpdateRequestV2
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(
            fcmTokenCommandUseCase.updateTopic(
                topicUpdateRequestV2.toCommand(fcmToken, type)
            ), "토픽 업데이트 성공"
        )

}