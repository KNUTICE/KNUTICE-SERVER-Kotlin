package com.fx.api.adapter.out.web.client

import io.github.seob7.Api
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader

/**
 * Crawler 서버로 알림 전송 요청을 담당하는 Feign Client
 *
 * @author 이동섭
 * @since 2025-10-22
 */
@FeignClient(name = "crawler-service", url = "\${crawler.server.address}")
interface NotificationFeignClient {

    /**
     * 특정 공지(nttId)에 대해 대상 사용자(fcmToken)에게 푸시 알림을 전송합니다.
     *
     * @param fcmToken 대상 사용자의 FCM 토큰
     * @param nttId 알림을 전송할 공지의 고유 ID
     * @return Api<Boolean> - 전송 성공 여부
     */
    @PostMapping("/open-api/v1/notification/notice/{nttId}")
    fun notifyNotice(
        @RequestHeader fcmToken: String,
        @PathVariable nttId: Long
    ): ResponseEntity<Api<Boolean>>

}