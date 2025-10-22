package com.fx.crawler.adapter.`in`.web

import com.fx.crawler.appllication.port.`in`.NotificationUseCase
import com.fx.crawler.appllication.port.`in`.dto.NoticeCommand
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import com.fx.global.domain.TopicType
import io.github.seob7.Api
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader

@WebInputAdapter
class NotificationApiAdapter(
    private val notificationUseCase: NotificationUseCase
) {

    private val log = LoggerFactory.getLogger(NotificationApiAdapter::class.java)

    @PostMapping("/open-api/v1/notification/notice/{nttId}")
    suspend fun pushTestNotice(
        @RequestHeader fcmToken: String,
        @PathVariable nttId: Long
    ): ResponseEntity<Api<Boolean>> {
        log.info("알림 발송 대상 fcmToken : {}", fcmToken)
        notificationUseCase.sendNotification(fcmToken, nttId)
        return Api.OK(true)
    }

}