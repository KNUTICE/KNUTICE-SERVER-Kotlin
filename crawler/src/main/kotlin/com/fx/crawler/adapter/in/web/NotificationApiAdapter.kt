package com.fx.crawler.adapter.`in`.web

import com.fx.common.annotation.hexagonal.WebInputAdapter
import com.fx.crawler.application.port.`in`.PushTestUseCase
import io.github.seob7.Api
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

/** api 서버(관리자)가 호출하는 테스트 발송. 토큰 하나에 실제와 같은 알림을 보낸다. */
@WebInputAdapter
@RequestMapping("/open-api/v1/notification")
class NotificationApiAdapter(
    private val pushTestUseCase: PushTestUseCase,
) {

    private val log = LoggerFactory.getLogger(NotificationApiAdapter::class.java)

    @PostMapping("/notice/{nttId}")
    fun pushTestNotice(
        @RequestHeader fcmToken: String,
        @PathVariable nttId: Long,
    ): ResponseEntity<Api<Boolean>> {
        log.info("공지 테스트 발송 - nttId: {}", nttId)
        pushTestUseCase.sendNotice(fcmToken, nttId)
        return Api.OK(true)
    }

    /** @param mealType 학식 토픽 이름 (예: `STUDENT_CAFETERIA`) */
    @PostMapping("/meal/{mealType}")
    fun pushTestMeal(
        @RequestHeader fcmToken: String,
        @PathVariable mealType: String,
    ): ResponseEntity<Api<Boolean>> {
        log.info("학식 테스트 발송 - topic: {}", mealType)
        pushTestUseCase.sendMeal(fcmToken, mealType)
        return Api.OK(true)
    }

}
