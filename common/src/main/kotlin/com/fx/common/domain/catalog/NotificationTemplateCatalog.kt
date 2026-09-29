package com.fx.common.domain.catalog

import com.fx.common.domain.i18n.Language
import com.fx.common.domain.i18n.LocalizedText
import com.fx.common.domain.notification.NotificationTemplateKey
import com.fx.common.domain.notification.TemplatePlaceholder

/** 알림 문구 전체의 불변 스냅샷. 발송할 때 언어별 문구를 만든다. */
class NotificationTemplateCatalog(
    private val texts: Map<NotificationTemplateKey, LocalizedText>,
) {

    fun contains(key: NotificationTemplateKey): Boolean = key in texts

    /**
     * [language] 문구를 골라 placeholder 를 [values] 로 채운다. 해당 언어 문구가 없으면 한국어를 쓴다.
     * @throws IllegalStateException DB 에 키가 없을 때 (Flyway 시드 누락)
     */
    fun render(key: NotificationTemplateKey, language: Language, values: Map<String, Any> = emptyMap()): String {
        val text = checkNotNull(texts[key]) { "알림 문구가 없습니다: $key" }
        return TemplatePlaceholder.fill(text.resolve(language), values)
    }

}
