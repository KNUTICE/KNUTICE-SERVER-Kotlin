package com.fx.crawler.domain.push

import com.fx.common.domain.i18n.Language

/**
 * 언어별로 만든 알림 목록. FCM multicast 한 번에는 문구가 하나이므로
 * 발송할 때 토큰을 언어별로 묶어 그 언어의 알림을 보낸다.
 */
data class LocalizedPushMessages(
    val messages: Map<Language, List<PushMessage>>,
) {

    /** 해당 언어 알림이 없으면 기본 언어(한국어) 알림을 쓴다. */
    fun of(language: Language): List<PushMessage> =
        messages[language] ?: messages[Language.DEFAULT].orEmpty()

    companion object {

        /** 지원하는 모든 언어로 알림을 만든다. */
        fun compose(block: (Language) -> List<PushMessage>): LocalizedPushMessages =
            LocalizedPushMessages(Language.entries.associateWith(block))

    }

}
