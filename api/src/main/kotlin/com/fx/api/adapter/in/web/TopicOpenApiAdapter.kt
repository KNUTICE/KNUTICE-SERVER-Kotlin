package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.topic.MajorTopicResponse
import com.fx.api.adapter.`in`.web.dto.topic.MajorTopicUpdateRequest
import com.fx.api.adapter.`in`.web.dto.topic.NoticeTopicUpdateRequest
import com.fx.api.adapter.`in`.web.dto.topic.NoticeTopicResponse
import com.fx.api.adapter.`in`.web.swagger.TopicOpenApiSwagger
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/topics")
class TopicOpenApiAdapter(
    private val fcmTokenQueryUseCase: FcmTokenQueryUseCase,
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase
) : TopicOpenApiSwagger {

    @GetMapping("/notice")
    override fun getMyNoticeTopics(@RequestHeader fcmToken: String): ResponseEntity<Api<NoticeTopicResponse>> =
        Api.OK(NoticeTopicResponse.from(fcmTokenQueryUseCase.getMyNoticeTopics(fcmToken)))

    @GetMapping("/major")
    override fun getMyMajorTopics(@RequestHeader fcmToken: String): ResponseEntity<Api<MajorTopicResponse>> =
        Api.OK(MajorTopicResponse.from(fcmTokenQueryUseCase.getMyMajorTopics(fcmToken)))

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


}