package com.fx.api.adapter.out.web

import com.fx.api.adapter.out.web.client.NotificationFeignClient
import com.fx.api.application.port.out.NotificationWebPort
import com.fx.api.domain.PushNotice
import com.fx.global.adapter.feign.NoticeNotificationFeignRequest
import com.fx.global.annotation.hexagonal.WebOutputAdapter

@WebOutputAdapter
class NotificationWebAdapter(
    private val notificationFeignClient: NotificationFeignClient
) : NotificationWebPort {

    override fun pushTestNotice(pushNotice: PushNotice): Boolean {
        val response = notificationFeignClient.pushTestNotice(pushNotice.fcmToken, createFeignRequestDto(pushNotice))
        return response.body?.metaData?.success ?: false
    }

    private fun createFeignRequestDto(pushNotice: PushNotice): NoticeNotificationFeignRequest =
        NoticeNotificationFeignRequest(
            nttId = pushNotice.nttId,
            title = pushNotice.title,
            department = pushNotice.department,
            contentUrl = pushNotice.contentUrl,
            contentImageUrl = pushNotice.contentImageUrl,
            registrationDate = pushNotice.registrationDate,
            isAttachment = pushNotice.isAttachment,
            topicType = pushNotice.topicType,
            topic = pushNotice.topic
        )

}