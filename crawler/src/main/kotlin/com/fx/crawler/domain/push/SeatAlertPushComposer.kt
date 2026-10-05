package com.fx.crawler.domain.push

import com.fx.common.domain.catalog.NotificationTemplateCatalog
import com.fx.common.domain.notification.NotificationTemplateKey
import com.fx.readingroom.domain.ReadingRoom

/** 열람실 빈자리 알림을 만든다. 열람실 이름은 번역하지 않는다. */
object SeatAlertPushComposer {

    fun compose(readingRoom: ReadingRoom, seatNumber: Int, templates: NotificationTemplateCatalog): LocalizedPushMessages =
        LocalizedPushMessages.compose { language ->
            listOf(
                PushMessage(
                    title = templates.render(NotificationTemplateKey.SEAT_ALERT_TITLE, language),
                    body = templates.render(
                        NotificationTemplateKey.SEAT_ALERT_BODY,
                        language,
                        mapOf("roomName" to readingRoom.roomName, "seatNumber" to seatNumber),
                    ),
                    data = mapOf("deeplink" to "knutice://reading-room?roomId=${readingRoom.name}&seat=$seatNumber"),
                )
            )
        }

}
