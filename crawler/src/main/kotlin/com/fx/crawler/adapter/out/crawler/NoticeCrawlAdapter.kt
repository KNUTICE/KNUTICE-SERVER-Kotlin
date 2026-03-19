package com.fx.crawler.adapter.out.crawler

import com.fx.crawler.appllication.port.out.NoticeCrawlPort
import com.fx.crawler.common.annotation.CrawlAdapter
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.Notice
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingle
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@CrawlAdapter
class NoticeCrawlAdapter(
    private val webClient: WebClient
): NoticeCrawlPort {

    private val log = LoggerFactory.getLogger(NoticeCrawlAdapter::class.java)

    override suspend fun crawlNoticeSummaries(topic: CrawlableType, page: Int): List<Notice> = coroutineScope {
        val pageUrl = topic.getNoticeUrl() + "?pageIndex=$page"

        val html = webClient.get()
            .uri(pageUrl)
            .retrieve()
            .bodyToMono<String>()
            .awaitSingle()

        val document = withContext(Dispatchers.Default) {
            Jsoup.parse(html)
        }

        val rows = document.select("table.basic_table tbody tr")

        val deferredList = rows.map { row ->
            async(Dispatchers.Default) {
                try {
                    val linkElement = row.selectFirst("td.left a") // [공지]
                    val formElement = row.selectFirst("td.left form") // 일반글

                    val (nttId, contentUrl, title) = when {
                        linkElement != null -> {
                            val pathUrl = linkElement.attr("href").trim()
                            val nttId = pathUrl.substringAfter("nttId=").substringBefore("&").toLongOrNull() ?: 0L
                            Triple(nttId, topic.rootDomain + pathUrl, linkElement.text().trim())
                        }
                        formElement != null -> {
                            val actionUrl = formElement.attr("action").trim()
                            val nttId = formElement.selectFirst("input[name=nttId]")?.attr("value")?.toLongOrNull() ?: 0L
                            val titleText = formElement.selectFirst("input[type=submit]")?.attr("value")?.trim().orEmpty()
                            Triple(nttId, topic.rootDomain + actionUrl + "?nttId=$nttId", titleText)
                        }
                        else -> return@async null
                    }

                    if (nttId == 0L || title.isBlank()) return@async null

                    val department = row.selectFirst("td.problem_name")?.text()?.trim().orEmpty()

                    val registrationDateStr = row.select("td.date").first()?.text()?.trim()
                    val registrationDate = registrationDateStr?.let {
                        LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    } ?: LocalDate.now()

                    val isAttachment = row.select("td.problem_file a").isNotEmpty()

                    Notice(
                        nttId = nttId,
                        title = title,
                        department = department,
                        contentUrl = contentUrl,
                        contentImageUrl = null, // 상세에서 채움
                        registrationDate = registrationDate,
                        isAttachment = isAttachment,
                        topic = topic,
                        createdAt = LocalDateTime.now()
                    )
                } catch (e: Exception) {
                    log.error("crawlNoticeSummaries error: ${e.message}", e)
                    null
                }
            }
        }

        deferredList.awaitAll().filterNotNull()
    }

    override suspend fun crawlNoticeDetails(notices: List<Notice>): List<Notice> = coroutineScope {
        val deferredList = notices.map { notice ->
            async(Dispatchers.IO) {
                try {
                    val html = webClient.get()
                        .uri(notice.contentUrl)
                        .retrieve()
                        .bodyToMono<String>()
                        .awaitSingle()

                    val detailDocument = withContext(Dispatchers.Default) {
                        Jsoup.parse(html)
                    }

                    // 이미지 추출
                    val rawImageUrl = detailDocument
                        .select("div.bbs_detail_content img")
                        .first()
                        ?.attr("src")

                    // http 인 경우 알림에 이미지 포함이 안되는 경우 발생
                    val contentFirstImageUrl = rawImageUrl?.let {
                        if (it.startsWith("http://")) {
                            "https://" + it.removePrefix("http://")
                        } else {
                            it
                        }
                    }

                    val content = detailDocument
                        .select("div.bbs_detail_content")
                        .text()
                        .trim()

                    notice.withDetail(content, contentFirstImageUrl)
                } catch (e: Exception) {
                    log.error("crawlNoticeDetails error for nttId=${notice.nttId}: ${e.message}", e)
                    notice
                }
            }
        }
        deferredList.awaitAll()
    }

}