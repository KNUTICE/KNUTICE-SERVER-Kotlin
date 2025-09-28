package com.fx.api.adapter.out.web.client

import com.fx.global.adapter.feign.NoticeNotificationFeignRequest
import io.github.seob7.Api
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "crawler-service", url = "\${crawler.server.address}")
interface NotificationFeignClient {

    @PostMapping("/open-api/v1/notification/notice")
    fun pushTestNotice(
        @RequestHeader fcmToken: String,
        @RequestBody notificationFeignRequest: NoticeNotificationFeignRequest
    ): ResponseEntity<Api<Boolean>>

}