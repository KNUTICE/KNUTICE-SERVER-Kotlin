package com.fx.common.domain.topic

import com.fx.common.domain.TopicType
import com.fx.common.domain.i18n.LocalizedText
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class TopicTest {

    private fun topic(
        topicType: TopicType = TopicType.MAJOR,
        name: String = "COMPUTER_SOFTWARE",
        collegeId: Long? = 1L,
    ) = Topic(
        code = 300,
        name = name,
        topicType = topicType,
        displayName = LocalizedText("컴퓨터소프트웨어학과"),
        collegeId = collegeId,
        rootDomain = "https://www.ut.ac.kr",
        bbsPath = "/cop/bbs/BBSMSTR/selectBoardList.do",
    )

    @Test
    fun `학과 토픽에는 단과대가 필요하다`() {
        assertThatThrownBy { topic(topicType = TopicType.MAJOR, collegeId = null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `공지 · 학식 토픽에는 단과대를 지정할 수 없다`() {
        assertThatThrownBy { topic(topicType = TopicType.NOTICE, collegeId = 1L) }
            .isInstanceOf(IllegalArgumentException::class.java)

        val notice = topic(topicType = TopicType.NOTICE, collegeId = null)
        assertThatThrownBy { notice.changeCollege(1L) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `토픽 이름은 v1 식별자 형식이어야 한다`() {
        assertThatThrownBy { topic(name = "computer-software") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `게시판 목록 URL 은 도메인과 경로를 합친다`() {
        assertThat(topic().noticeUrl()).isEqualTo("https://www.ut.ac.kr/cop/bbs/BBSMSTR/selectBoardList.do")
    }

}
