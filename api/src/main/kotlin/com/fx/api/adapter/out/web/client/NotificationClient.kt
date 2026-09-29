package com.fx.api.adapter.out.web.client

import com.fx.common.domain.MealType
import io.github.seob7.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

/**
 * Crawler 서버로 알림 전송 요청을 담당하는 HTTP Interface Client
 * base-url 은 `spring.http.serviceclient.crawler.base-url` 로 설정한다. ([com.fx.api.config.web.HttpServiceConfig])
 *
 * @author 이동섭
 * @since 2025-10-22
 * @update
 * - (2026-09-29) : OpenFeign → Spring HTTP Interface 로 변경
 */
@HttpExchange("/open-api/v1/notification")
interface NotificationClient {

    /**
     * 특정 공지(nttId)에 대해 대상 사용자(fcmToken)에게 푸시 알림을 전송합니다.
     *
     * @param fcmToken 대상 사용자의 FCM 토큰
     * @param nttId 알림을 전송할 공지의 고유 ID
     * @return Api<Boolean> - 전송 성공 여부
     */
    @PostExchange("/notice/{nttId}")
    fun notifyNotice(
        @RequestHeader("fcmToken") fcmToken: String,
        @PathVariable("nttId") nttId: Long
    ): ResponseEntity<Api<Boolean>>

    /**
     * 특정 학식에 대해 대상 사용자(fcmToken)에게 푸시 알림을 전송합니다.
     *
     * @param fcmToken 대상 사용자의 FCM 토큰
     * @param mealType 알림을 전송할 학식 종류
     * @return Api<Boolean> - 전송 성공 여부
     */
    @PostExchange("/meal/{mealType}")
    fun notifyMeal(
        @RequestHeader("fcmToken") fcmToken: String,
        @PathVariable("mealType") mealType: MealType
    ): ResponseEntity<Api<Boolean>>

}
