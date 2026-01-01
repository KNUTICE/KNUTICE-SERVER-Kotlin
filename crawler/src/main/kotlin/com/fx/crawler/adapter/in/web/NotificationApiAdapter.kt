package com.fx.crawler.adapter.`in`.web

import com.fx.crawler.appllication.port.`in`.MealNotificationUseCase
import com.fx.crawler.appllication.port.`in`.MealParseUseCase
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
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/notification")
class NotificationApiAdapter(
    private val notificationUseCase: NotificationUseCase,
    private val mealParseUseCase: MealParseUseCase,
    private val mealNotificationUseCase: MealNotificationUseCase
) {

    private val log = LoggerFactory.getLogger(NotificationApiAdapter::class.java)

    @PostMapping("|/notice/{nttId}")
    suspend fun pushTestNotice(
        @RequestHeader fcmToken: String,
        @PathVariable nttId: Long
    ): ResponseEntity<Api<Boolean>> {
        log.info("알림 발송 대상 fcmToken : {}", fcmToken)
        notificationUseCase.sendNotification(fcmToken, nttId)
        return Api.OK(true)
    }

    @PostMapping("/meal/{mealType}")
    suspend fun pushTestMeal(
        @RequestHeader fcmToken: String,
        @PathVariable mealType: MealType
    ): ResponseEntity<Api<Boolean>> {
        log.info("알림 발송 대상 fcmToken : {}", fcmToken)
        val meals = mealParseUseCase.parseMeals(listOf(mealType))
        mealNotificationUseCase.sendNotification(meals);
        return Api.OK(true)
    }

}