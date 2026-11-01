package com.fx.api.adapter.`in`.web.template.dto

import com.fx.common.domain.i18n.LocalizedText
import com.fx.common.domain.notification.NotificationTemplate
import com.fx.common.domain.notification.NotificationTemplateKey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class NotificationTemplateResponseTest : BehaviorSpec({

    Given("알림 문구를 응답으로 바꾸면") {
        val template = NotificationTemplate(
            NotificationTemplateKey.NOTICE_BODY_MULTIPLE,
            LocalizedText("{title} 외 {count}개의 소식이 있습니다.", "{title} and {count} more updates."),
            "공지 3건 이상을 한 알림으로 보낼 때의 본문",
        )

        When("문구에 placeholder 가 있으면") {
            val response = NotificationTemplateResponse.from(template)

            Then("키에 정의된 placeholder 를 정의 순서대로 담는다") {
                response.placeholders shouldBe listOf("title", "count")
            }

            Then("언어별 문구를 담고, 없는 언어는 null 로 둔다") {
                response.text shouldBe NotificationTemplateResponse.TextResponse(
                    "{title} 외 {count}개의 소식이 있습니다.",
                    "{title} and {count} more updates.",
                    null,
                )
            }
        }

        When("placeholder 가 없는 문구면") {
            val response = NotificationTemplateResponse.from(
                NotificationTemplate(NotificationTemplateKey.SEAT_ALERT_TITLE, LocalizedText("빈자리 알림"), "열람실 빈자리 알림 제목")
            )

            Then("빈 목록을 담는다") {
                response.placeholders.shouldBeEmpty()
            }
        }
    }

})
