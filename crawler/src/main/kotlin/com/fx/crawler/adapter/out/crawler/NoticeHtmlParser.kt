package com.fx.crawler.adapter.out.crawler

import com.fx.common.domain.catalog.TopicView
import com.fx.crawler.domain.crawl.CrawledNotice
import com.fx.crawler.domain.crawl.NoticeDetail
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 학교 게시판 HTML 파서. 모든 게시판이 같은 템플릿(`table.basic_table`)을 쓴다.
 *
 * 목록의 글은 두 형태다.
 * - 상단 고정 [공지] 글 : `td.left a[href*=nttId=]`
 * - 일반 글 : `td.left form` 안의 `input[name=nttId]` · `input[type=submit]` (제목)
 */
object NoticeHtmlParser {

    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** @param today 게시일을 읽지 못한 글의 게시일 */
    fun parseList(document: Document, topic: TopicView, today: LocalDate): List<CrawledNotice> =
        document.select("table.basic_table tbody tr").mapNotNull { row ->
            parseRow(row, topic, today)
        }

    fun parseDetail(document: Document): NoticeDetail {
        val content = document.select("div.bbs_detail_content")
        val imageUrl = content.select("img").first()?.attr("src")?.takeIf {
            it.isNotBlank()
        }
        return NoticeDetail(
            content = content.text().trim().takeIf {
                it.isNotEmpty()
            },
            // http 이미지는 알림에 표시되지 않는다
            contentImageUrl = imageUrl?.let {
                if (it.startsWith("http://")) "https://" + it.removePrefix("http://") else it
            },
        )
    }

    private fun parseRow(row: Element, topic: TopicView, today: LocalDate): CrawledNotice? {
        val link = row.selectFirst("td.left a")
        val form = row.selectFirst("td.left form")

        val (nttId, contentUrl, title) = when {
            link != null -> {
                val path = link.attr("href").trim()
                Triple(
                    path.substringAfter("nttId=").substringBefore("&").toLongOrNull(),
                    topic.rootDomain + path,
                    link.text().trim(),
                )
            }
            form != null -> {
                val nttId = form.selectFirst("input[name=nttId]")?.attr("value")?.toLongOrNull()
                Triple(
                    nttId,
                    topic.rootDomain + form.attr("action").trim() + "?nttId=$nttId",
                    form.selectFirst("input[type=submit]")?.attr("value")?.trim().orEmpty(),
                )
            }
            else -> return null
        }
        if (nttId == null || nttId <= 0 || title.isBlank()) {
            return null
        }

        return CrawledNotice(
            nttId = nttId,
            topic = topic,
            title = title,
            department = row.selectFirst("td.problem_name")?.text()?.trim().orEmpty(),
            contentUrl = contentUrl,
            registrationDate = parseDate(row.selectFirst("td.date")?.text()?.trim()) ?: today,
            isAttachment = row.select("td.problem_file a").isNotEmpty(),
        )
    }

    private fun parseDate(text: String?): LocalDate? =
        try {
            text?.let {
                LocalDate.parse(it, DATE_FORMAT)
            }
        } catch (e: DateTimeParseException) {
            null
        }

}
