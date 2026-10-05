package com.fx.common.domain.notification

import com.fx.common.domain.catalog.NotificationTemplateCatalog
import com.fx.common.domain.i18n.Language
import com.fx.common.domain.i18n.LocalizedText
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class NotificationTemplateTest {

    private val body = LocalizedText(
        ko = "{title} 외 {count}개의 소식이 있습니다.",
        en = "{title} and {count} more",
    )

    @Test
    fun `키의 placeholder 를 모두 쓰는 문구는 저장할 수 있다`() {
        val template = NotificationTemplate(NotificationTemplateKey.NOTICE_BODY_MULTIPLE, body, "설명")

        assertThat(template.text).isEqualTo(body)
    }

    @Test
    fun `placeholder 가 빠진 문구는 거부한다`() {
        val missing = LocalizedText(ko = "{title} 외 소식이 있습니다.")

        assertThatThrownBy {
            NotificationTemplate(NotificationTemplateKey.NOTICE_BODY_MULTIPLE, missing, "설명")
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("count")
    }

    @Test
    fun `모르는 placeholder 가 있는 번역은 거부한다`() {
        val template = NotificationTemplate(NotificationTemplateKey.SEAT_ALERT_TITLE, LocalizedText("빈자리 알림"), "설명")

        assertThatThrownBy {
            template.changeText(LocalizedText(ko = "빈자리 알림", ja = "{seatNumber} 空席"))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("seatNumber")
        assertThat(template.text).isEqualTo(LocalizedText("빈자리 알림"))
    }

    @Test
    fun `언어별 문구를 골라 placeholder 를 채운다`() {
        val catalog = NotificationTemplateCatalog(mapOf(NotificationTemplateKey.NOTICE_BODY_MULTIPLE to body))
        val values = mapOf("title" to "장학 공지", "count" to 2)

        assertThat(catalog.render(NotificationTemplateKey.NOTICE_BODY_MULTIPLE, Language.KO, values))
            .isEqualTo("장학 공지 외 2개의 소식이 있습니다.")
        assertThat(catalog.render(NotificationTemplateKey.NOTICE_BODY_MULTIPLE, Language.EN, values))
            .isEqualTo("장학 공지 and 2 more")
        // 일본어 문구가 없으면 한국어로 보낸다
        assertThat(catalog.render(NotificationTemplateKey.NOTICE_BODY_MULTIPLE, Language.JA, values))
            .isEqualTo("장학 공지 외 2개의 소식이 있습니다.")
    }

    @Test
    fun `DB 에 없는 키로 문구를 만들면 실패한다`() {
        val catalog = NotificationTemplateCatalog(emptyMap())

        assertThatThrownBy {
            catalog.render(NotificationTemplateKey.MEAL_EMPTY, Language.KO)
        }.isInstanceOf(IllegalStateException::class.java)
    }

}
