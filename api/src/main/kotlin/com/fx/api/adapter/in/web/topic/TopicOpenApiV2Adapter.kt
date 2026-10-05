package com.fx.api.adapter.`in`.web.topic

import com.fx.api.adapter.`in`.web.topic.dto.TopicResponseV2
import com.fx.api.adapter.`in`.web.topic.dto.TopicUpdateRequestV2
import com.fx.api.application.port.`in`.fcmtoken.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.fcmtoken.FcmTokenQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import com.fx.common.domain.TopicType
import com.fx.common.domain.i18n.Language
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
@RequestMapping("/open-api/v2/topics")
class TopicOpenApiV2Adapter(
    private val fcmTokenQueryUseCase: FcmTokenQueryUseCase,
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase,
) : TopicOpenApiV2Swagger {

    @GetMapping
    override fun getMyTopics(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType,
        locale: Locale
    ): ResponseEntity<Api<TopicResponseV2>> =
        Api.OK(
            TopicResponseV2.from(fcmTokenQueryUseCase.getMyTopics(fcmToken, type), Language.from(locale)),
            "토픽 조회 성공"
        )

    @PatchMapping
    override fun updateTopic(
        @RequestHeader fcmToken: String,
        @RequestParam type: TopicType,
        @RequestBody @Valid topicUpdateRequestV2: TopicUpdateRequestV2
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(
            fcmTokenCommandUseCase.updateTopic(topicUpdateRequestV2.toCommand(fcmToken, type)),
            "토픽 업데이트 성공"
        )

}
