package com.fx.crawler.domain.push

import com.fx.common.domain.catalog.NotificationTemplateCatalog
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notification.NotificationTemplateKey

/**
 * 공지 알림을 만든다. 제목은 토픽 표시명이고, 본문은 공지 제목(학교 원문, 번역하지 않음)이다.
 *
 * 한 토픽에 새 공지가 1~2건이면 공지마다 알림을 하나씩, 3건 이상이면
 * 첫 공지 제목에 "외 N개의 소식" 을 붙인 알림 하나로 보낸다.
 */
object NoticePushComposer {

    private const val GROUPING_THRESHOLD = 3

    /** @param notices 같은 토픽의 공지. 앞의 공지가 묶음 알림의 대표가 된다. */
    fun compose(topic: TopicView, notices: List<Notice>, templates: NotificationTemplateCatalog): LocalizedPushMessages {
        require(notices.isNotEmpty()) { "알림을 만들 공지가 없습니다." }

        return LocalizedPushMessages.compose { language ->
            val title = topic.displayName.resolve(language)
            if (notices.size < GROUPING_THRESHOLD) {
                notices.map { message(title, it.title, it) }
            } else {
                val first = notices.first()
                val body = templates.render(
                    NotificationTemplateKey.NOTICE_BODY_MULTIPLE,
                    language,
                    mapOf("title" to first.title, "count" to notices.size - 1),
                )
                listOf(message(title, body, first))
            }
        }
    }

    private fun message(title: String, body: String, notice: Notice): PushMessage =
        PushMessage(
            title = title,
            body = body,
            imageUrl = notice.contentImageUrl,
            data = mapOf(
                // 1.6.x 이상 앱
                "deeplink" to "knutice://notice?nttId=${notice.nttId}&contentUrl=${notice.contentUrl}&FabVisible=true",
                // 1.5.x 이하 앱. Android 딥링크 인코딩 문제로 nttId · contentUrl 을 따로 보낸다
                "nttId" to notice.nttId.toString(),
                "contentUrl" to notice.contentUrl,
            ),
        )

}
