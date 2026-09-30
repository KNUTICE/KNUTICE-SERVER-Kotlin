package com.fx.crawler.domain.push

import com.fx.common.domain.i18n.Language
import com.fx.crawler.fixture.CrawlerFixture
import com.fx.crawler.fixture.CrawlerFixture.GENERAL_NEWS
import com.fx.crawler.fixture.CrawlerFixture.TEMPLATES
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.groups.Tuple.tuple
import org.junit.jupiter.api.Test

class NoticePushComposerTest {

    @Test
    fun `공지가 2건 이하면 공지마다 알림을 하나씩 만든다`() {
        val notices = listOf(CrawlerFixture.notice(2, "수강신청 안내", imageUrl = "https://img/2.png"), CrawlerFixture.notice(1, "장학 안내"))

        val messages = NoticePushComposer.compose(GENERAL_NEWS, notices, TEMPLATES).of(Language.KO)

        assertThat(messages).extracting("title", "body", "imageUrl").containsExactly(
            tuple("일반소식", "수강신청 안내", "https://img/2.png"),
            tuple("일반소식", "장학 안내", null),
        )
        assertThat(messages[0].data).containsExactlyInAnyOrderEntriesOf(
            mapOf(
                "deeplink" to "knutice://notice?nttId=2&contentUrl=https://www.ut.ac.kr/notice?nttId=2&FabVisible=true",
                "nttId" to "2",
                "contentUrl" to "https://www.ut.ac.kr/notice?nttId=2",
            )
        )
    }

    @Test
    fun `공지가 3건 이상이면 첫 공지를 대표로 한 알림 하나로 묶는다`() {
        val notices = listOf(CrawlerFixture.notice(3, "첫 공지"), CrawlerFixture.notice(2), CrawlerFixture.notice(1))

        val messages = NoticePushComposer.compose(GENERAL_NEWS, notices, TEMPLATES)

        assertThat(messages.of(Language.KO)).singleElement().satisfies({
            assertThat(it.title).isEqualTo("일반소식")
            assertThat(it.body).isEqualTo("첫 공지 외 2개의 소식이 있습니다.")
            assertThat(it.data["nttId"]).isEqualTo("3")
        })
        assertThat(messages.of(Language.EN).single().body).isEqualTo("첫 공지 and 2 more")
    }

    @Test
    fun `제목은 토픽 표시명을 언어별로 쓰고 공지 제목은 번역하지 않는다`() {
        val messages = NoticePushComposer.compose(GENERAL_NEWS, listOf(CrawlerFixture.notice(1, "수강신청 안내")), TEMPLATES)

        assertThat(messages.of(Language.EN).single().title).isEqualTo("General News")
        assertThat(messages.of(Language.JA).single().title).isEqualTo("一般ニュース")
        assertThat(messages.of(Language.JA).single().body).isEqualTo("수강신청 안내")
    }

    @Test
    fun `번역이 없는 문구는 한국어로 보낸다`() {
        val notices = listOf(CrawlerFixture.notice(3, "첫 공지"), CrawlerFixture.notice(2), CrawlerFixture.notice(1))

        assertThat(NoticePushComposer.compose(GENERAL_NEWS, notices, TEMPLATES).of(Language.JA).single().body)
            .isEqualTo("첫 공지 외 2개의 소식이 있습니다.")
    }

    @Test
    fun `공지가 없으면 만들 수 없다`() {
        assertThatThrownBy {
            NoticePushComposer.compose(GENERAL_NEWS, emptyList(), TEMPLATES)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

}
