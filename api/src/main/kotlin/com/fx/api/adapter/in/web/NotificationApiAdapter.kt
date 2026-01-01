package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.swagger.NotificationApiSwagger
import com.fx.api.application.port.`in`.NotificationUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.domain.MealType
import io.github.seob7.Api
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/api/v1/notification")
class NotificationApiAdapter(
    private val notificationUseCase: NotificationUseCase
) : NotificationApiSwagger {

    private val log = LoggerFactory.getLogger(NoticeOpenApiAdapter::class.java)

    @PostMapping("/notices/{nttId}")
    override fun notifyNotice(
        @RequestHeader fcmToken: String,
        @PathVariable nttId: Long
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(notificationUseCase.notifyNotice(fcmToken, nttId)
            , "알림이 발송되었습니다.")


    @PostMapping("/meals/{mealType}")
    override fun notifyMeal(
        @RequestHeader fcmToken: String,
        @PathVariable mealType: MealType
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(notificationUseCase.notifyMeal(fcmToken, mealType)
            , "알림이 발송되었습니다.")

}