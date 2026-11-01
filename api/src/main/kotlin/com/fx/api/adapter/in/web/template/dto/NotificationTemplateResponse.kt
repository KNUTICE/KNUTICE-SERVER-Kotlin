package com.fx.api.adapter.`in`.web.template.dto

import com.fx.common.domain.notification.NotificationTemplate
import com.fx.common.domain.notification.NotificationTemplateKey

data class NotificationTemplateResponse(
    val templateKey: NotificationTemplateKey,
    val text: TextResponse,
    /** 문구가 쓰이는 곳 */
    val description: String,
    /** 문구에 반드시 들어가야 하는 placeholder 이름. [NotificationTemplateKey] 에 정의된 순서 */
    val placeholders: List<String>,
) {

    /** `ko` 는 항상 있고 `en` · `ja` 는 null 일 수 있다. */
    data class TextResponse(
        val ko: String,
        val en: String?,
        val ja: String?,
    )

    companion object {

        fun from(template: NotificationTemplate): NotificationTemplateResponse =
            NotificationTemplateResponse(
                templateKey = template.templateKey,
                text = TextResponse(template.text.ko, template.text.en, template.text.ja),
                description = template.description,
                placeholders = template.templateKey.placeholders.toList(),
            )

        fun from(templates: List<NotificationTemplate>): List<NotificationTemplateResponse> =
            templates.map {
                from(it)
            }

    }

}
