package com.fx.crawler.adapter.out.crawler

import com.fx.crawler.fixture.CrawlerFixture.GENERAL_NEWS
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.time.LocalDate

class NoticeHtmlParserTest {

    private val today = LocalDate.of(2026, 9, 30)

    @Test
    fun `목록에서 상단 고정 글과 일반 글을 읽는다`() {
        val html = """
            <table class="basic_table"><tbody>
              <tr>
                <td class="left"><a href="/cop/bbs/BBSMSTR_59/selectBoardArticle.do?nttId=1001&amp;bbsId=BBSMSTR_59">[공지] 수강신청 안내</a></td>
                <td class="problem_name">학사팀</td>
                <td class="problem_file"><a href="/file">첨부</a></td>
                <td class="date">2026-09-29</td>
              </tr>
              <tr>
                <td class="left">
                  <form action="/cop/bbs/BBSMSTR_59/selectBoardArticle.do" method="post">
                    <input type="hidden" name="nttId" value="1002"><input type="submit" value=" 장학금 신청 ">
                  </form>
                </td>
                <td class="problem_name">학생지원팀</td>
                <td class="problem_file"></td>
                <td class="date">-</td>
              </tr>
              <tr><td class="left"><span>링크 없는 행</span></td></tr>
              <tr><td class="left"><form action="/x"><input type="hidden" name="nttId" value="0"><input type="submit" value="번호 없는 글"></form></td></tr>
            </tbody></table>
        """.trimIndent()

        val notices = NoticeHtmlParser.parseList(Jsoup.parse(html), GENERAL_NEWS, today)

        assertThat(notices).hasSize(2)
        assertThat(notices[0]).satisfies({
            assertThat(it.nttId).isEqualTo(1001L)
            assertThat(it.title).isEqualTo("[공지] 수강신청 안내")
            assertThat(it.contentUrl).isEqualTo("https://www.ut.ac.kr/cop/bbs/BBSMSTR_59/selectBoardArticle.do?nttId=1001&bbsId=BBSMSTR_59")
            assertThat(it.department).isEqualTo("학사팀")
            assertThat(it.isAttachment).isTrue()
            assertThat(it.registrationDate).isEqualTo(LocalDate.of(2026, 9, 29))
            assertThat(it.topic).isEqualTo(GENERAL_NEWS)
        })
        assertThat(notices[1]).satisfies({
            assertThat(it.nttId).isEqualTo(1002L)
            assertThat(it.title).isEqualTo("장학금 신청")
            assertThat(it.contentUrl).isEqualTo("https://www.ut.ac.kr/cop/bbs/BBSMSTR_59/selectBoardArticle.do?nttId=1002")
            assertThat(it.isAttachment).isFalse()
            // 게시일을 읽지 못하면 오늘로 둔다
            assertThat(it.registrationDate).isEqualTo(today)
        })
    }

    @Test
    fun `상세에서 본문 텍스트와 첫 이미지를 읽고 http 이미지는 https 로 바꾼다`() {
        val html = """
            <div class="bbs_detail_content">
              <p>본문 첫 줄</p>
              <img src="http://www.ut.ac.kr/upload/a.png"><img src="https://www.ut.ac.kr/upload/b.png">
              <p>둘째 줄</p>
            </div>
        """.trimIndent()

        val detail = NoticeHtmlParser.parseDetail(Jsoup.parse(html))

        assertThat(detail.content).isEqualTo("본문 첫 줄 둘째 줄")
        assertThat(detail.contentImageUrl).isEqualTo("https://www.ut.ac.kr/upload/a.png")
    }

    @Test
    fun `본문이 없으면 비워 둔다`() {
        val detail = NoticeHtmlParser.parseDetail(Jsoup.parse("<div class='other'>내용</div>"))

        assertThat(detail.content).isNull()
        assertThat(detail.contentImageUrl).isNull()
    }

}
