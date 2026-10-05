package com.fx.crawler.domain.push

import com.fx.common.domain.catalog.NotificationTemplateCatalog
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.i18n.Language
import com.fx.common.domain.notification.NotificationTemplateKey
import com.fx.crawler.domain.meal.Meal

/**
 * 학식 알림을 만든다. 제목은 식당(토픽) 표시명이고, 본문은 머리말 · 구분 제목(번역) 아래에 메뉴(원문)를 줄마다 쓴다.
 *
 * ```
 * 2026-09-30 학생식당 메뉴
 * [한식]
 * 김치찌개
 *
 * [일품]
 * 돈까스
 * ```
 */
object MealPushComposer {

    fun compose(topic: TopicView, meal: Meal, templates: NotificationTemplateCatalog): LocalizedPushMessages =
        LocalizedPushMessages.compose { language ->
            val mealName = topic.displayName.resolve(language)
            listOf(
                PushMessage(
                    title = mealName,
                    body = body(meal, mealName, language, templates),
                    data = mapOf(
                        "deeplink" to "knutice://meal?topic=${topic.name}",
                        "topic" to topic.name,
                    ),
                )
            )
        }

    private fun body(meal: Meal, mealName: String, language: Language, templates: NotificationTemplateCatalog): String {
        val header = templates.render(
            NotificationTemplateKey.MEAL_HEADER,
            language,
            mapOf("date" to meal.mealDate, "mealName" to mealName),
        )

        val lines = mutableListOf<String>()
        if (meal.koreaMenus.isNotEmpty()) {
            lines += templates.render(NotificationTemplateKey.MEAL_SECTION_KOREAN, language)
            lines += meal.koreaMenus
        }
        if (meal.topMenus.isNotEmpty()) {
            if (lines.isNotEmpty()) lines += ""
            lines += templates.render(NotificationTemplateKey.MEAL_SECTION_TOP, language)
            lines += meal.topMenus
        }

        val menuText = if (lines.isEmpty()) {
            templates.render(NotificationTemplateKey.MEAL_EMPTY, language)
        } else {
            lines.joinToString("\n")
        }
        return "$header\n$menuText"
    }

}
