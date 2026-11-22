package com.fx.crawler.domain.embedding

import com.fx.common.domain.notice.NOTICE_URL_MAX_LENGTH
import com.fx.common.domain.notice.NoticeChunkType

/** 임베딩 전의 청크 한 개. [index] 는 공지 안에서의 순서(0부터)다. */
data class ChunkDraft(
    val index: Int,
    val type: NoticeChunkType,
    val content: String,
    val imageUrl: String? = null,
)

/**
 * 공지를 임베딩 청크로 나눈다.
 *
 * - 본문은 약 [CHUNK_SIZE] 자씩 자르고 앞 조각과 [OVERLAP] 자 겹친다. 자르는 자리 앞 [CUT_WINDOW] 자 안에 줄바꿈 · 마침표 · 공백이 있으면 거기서 끊는다
 * - 조각마다 앞에 `[게시판] 제목` 을 붙여, 조각만 봐도 어느 공지인지 알 수 있게 한다
 * - 이미지는 한 장에 한 청크. 벡터는 이미지로 만들고 내용에는 `[게시판] 제목` 만 둔다
 * - 본문 · 이미지가 모두 없으면 `[게시판] 제목` 하나로 TITLE 청크를 만든다
 */
object NoticeChunker {

    const val CHUNK_SIZE = 700
    const val OVERLAP = 120
    private const val CUT_WINDOW = 100

    private val SPACES = Regex("[ \\t]+")
    private val BLANK_LINES = Regex("\\n\\s*\\n+")

    fun chunk(topicName: String, title: String, body: String?, imageUrls: List<String>): List<ChunkDraft> {
        val header = "[$topicName] $title"
        val drafts = splitBody(clean(body)).map {
            NoticeChunkType.BODY to "$header\n$it"
        } + imageUrls.filter {
            it.length <= NOTICE_URL_MAX_LENGTH
        }.distinct().map {
            NoticeChunkType.IMAGE to it
        }

        if (drafts.isEmpty()) {
            return listOf(ChunkDraft(0, NoticeChunkType.TITLE, header))
        }
        return drafts.mapIndexed { index, (type, value) ->
            when (type) {
                NoticeChunkType.IMAGE -> ChunkDraft(index, type, header, imageUrl = value)
                else -> ChunkDraft(index, type, value)
            }
        }
    }

    internal fun clean(text: String?): String =
        (text ?: "")
            .replace("\r", "\n")
            .replace(' ', ' ')
            .replace(SPACES, " ")
            .replace(BLANK_LINES, "\n")
            .trim()

    internal fun splitBody(body: String): List<String> {
        if (body.length <= CHUNK_SIZE) {
            return listOf(body).filter {
                it.isNotBlank()
            }
        }
        val pieces = mutableListOf<String>()
        var start = 0
        while (true) {
            var end = minOf(start + CHUNK_SIZE, body.length)
            if (end < body.length) {
                val window = body.substring(end - CUT_WINDOW, end)
                val cut = maxOf(window.lastIndexOf('\n'), window.lastIndexOf(". "), window.lastIndexOf(' '))
                if (cut > 0) {
                    end = end - CUT_WINDOW + cut + 1
                }
            }
            pieces += body.substring(start, end).trim()
            if (end >= body.length) {
                break
            }
            start = end - OVERLAP
        }
        return pieces.filter {
            it.isNotBlank()
        }
    }

}
