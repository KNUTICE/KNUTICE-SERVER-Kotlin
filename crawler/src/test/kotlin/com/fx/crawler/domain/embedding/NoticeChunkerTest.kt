package com.fx.crawler.domain.embedding

import com.fx.common.domain.notice.NoticeChunkType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NoticeChunkerTest {

    @Test
    fun `짧은 본문은 한 조각, 이미지는 장마다 한 청크로 본문 뒤에 붙는다`() {
        val chunks = NoticeChunker.chunk(
            topicName = "일반소식",
            title = "동계 현장실습 모집",
            body = "모집 기간은 11월 20일까지입니다.",
            imageUrls = listOf("https://www.ut.ac.kr/upload/a.png", "https://www.ut.ac.kr/upload/b.png"),
        )

        assertThat(chunks).containsExactly(
            ChunkDraft(0, NoticeChunkType.BODY, "[일반소식] 동계 현장실습 모집\n모집 기간은 11월 20일까지입니다."),
            ChunkDraft(1, NoticeChunkType.IMAGE, "[일반소식] 동계 현장실습 모집", "https://www.ut.ac.kr/upload/a.png"),
            ChunkDraft(2, NoticeChunkType.IMAGE, "[일반소식] 동계 현장실습 모집", "https://www.ut.ac.kr/upload/b.png"),
        )
    }

    @Test
    fun `본문과 이미지가 모두 없으면 제목 청크 하나만 만든다`() {
        val chunks = NoticeChunker.chunk("학사공지", "휴강 안내", body = "  \n ", imageUrls = emptyList())

        assertThat(chunks).containsExactly(ChunkDraft(0, NoticeChunkType.TITLE, "[학사공지] 휴강 안내"))
    }

    @Test
    fun `본문이 없어도 이미지가 있으면 이미지 청크만 만든다`() {
        val chunks = NoticeChunker.chunk("학사공지", "포스터", body = null, imageUrls = listOf("https://www.ut.ac.kr/upload/p.png"))

        assertThat(chunks.map { it.type }).containsExactly(NoticeChunkType.IMAGE)
    }

    @Test
    fun `긴 본문은 700자 안쪽으로 자르고 앞 조각과 겹치게 이어 붙인다`() {
        val body = (1..400).joinToString(" ") { "문장$it" }

        val pieces = NoticeChunker.splitBody(body)

        assertThat(pieces).hasSizeGreaterThan(1)
        assertThat(pieces).allSatisfy {
            assertThat(it.length).isLessThanOrEqualTo(NoticeChunker.CHUNK_SIZE)
        }
        // 다음 조각의 첫 단어는 앞 조각 안에 있다 (겹침)
        pieces.zipWithNext().forEach { (prev, next) ->
            assertThat(prev).contains(next.substringBefore(" "))
        }
        assertThat(pieces.first()).startsWith("문장1 ")
        assertThat(pieces.last()).endsWith("문장400")
    }

    @Test
    fun `공백 단위로 끊어 단어 중간에서 자르지 않는다`() {
        val body = (1..400).joinToString(" ") { "문장$it" }

        NoticeChunker.splitBody(body).forEach { piece ->
            piece.split(" ").forEach {
                assertThat(it).matches("문장\\d+")
            }
        }
    }

    @Test
    fun `본문의 줄바꿈 · 공백을 정리한다`() {
        assertThat(NoticeChunker.clean("가\r\n\n\n나  다\t\t라  ")).isEqualTo("가\n나 다 라")
    }

    @Test
    fun `같은 이미지와 컬럼보다 긴 이미지 주소는 뺀다`() {
        val tooLong = "https://www.ut.ac.kr/" + "a".repeat(1000)

        val chunks = NoticeChunker.chunk(
            "학사공지", "포스터", body = null,
            imageUrls = listOf("https://www.ut.ac.kr/upload/p.png", tooLong, "https://www.ut.ac.kr/upload/p.png"),
        )

        assertThat(chunks.map { it.imageUrl }).containsExactly("https://www.ut.ac.kr/upload/p.png")
    }

}
