package com.fx.crawler.domain.push

import com.fx.common.domain.i18n.Language
import com.fx.crawler.fixture.CrawlerFixture.TEMPLATES
import com.fx.readingroom.domain.ReadingRoom
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SeatAlertPushComposerTest {

    @Test
    fun `열람실 이름은 번역하지 않고 문구만 언어별로 만든다`() {
        val messages = SeatAlertPushComposer.compose(ReadingRoom.ROOM1, 12, TEMPLATES)

        assertThat(messages.of(Language.KO).single()).satisfies({
            assertThat(it.title).isEqualTo("빈자리 알림")
            assertThat(it.body).isEqualTo("제1집중 학습 ZONE 12번 좌석이 비었습니다!")
            assertThat(it.data).containsEntry("deeplink", "knutice://reading-room?roomId=ROOM1&seat=12")
        })
        assertThat(messages.of(Language.EN).single().body).isEqualTo("Seat 12 in 제1집중 학습 ZONE is available!")
        assertThat(messages.of(Language.JA).single().title).isEqualTo("空席のお知らせ")
        assertThat(messages.of(Language.JA).single().body).isEqualTo("제1집중 학습 ZONE 12번 좌석이 비었습니다!")
    }

}
