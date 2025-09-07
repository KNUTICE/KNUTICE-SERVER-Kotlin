package com.fx.api.adapter.out.web

import com.fx.api.adapter.out.web.client.NotificationFeignClient
import com.fx.api.application.port.out.NotificationWebPort
import com.fx.global.adapter.feign.NoticeNotificationFeignRequest
import com.fx.global.annotation.hexagonal.WebOutputAdapter
import com.fx.global.domain.Notice
import com.fx.global.domain.TopicType

@WebOutputAdapter
class NotificationWebAdapter(
    private val notificationFeignClient: NotificationFeignClient
) : NotificationWebPort {

    override fun pushTestNotice(fcmToken: String, topicType: TopicType, notice: Notice): Boolean {
        val response = notificationFeignClient.pushTestNotice(fcmToken, createFeignRequestDto(notice, topicType))
        return response.body?.success ?: false
    }

    private fun createFeignRequestDto(notice: Notice, topicType: TopicType): NoticeNotificationFeignRequest =
        NoticeNotificationFeignRequest(
            nttId = notice.nttId,
            title = notice.title,
            department = notice.department,
            contentUrl = notice.contentUrl,
            contentImageUrl = notice.contentImageUrl,
            registrationDate = notice.registrationDate,
            isAttachment = notice.isAttachment,
            topicType = topicType,
            type = notice.type.toString()
        )

}