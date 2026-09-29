package com.fx.crawler.adapter.out.crawler

import com.fx.common.domain.catalog.TopicView
import com.fx.crawler.application.port.out.NoticeCrawlPort
import com.fx.crawler.common.annotation.CrawlAdapter
import com.fx.crawler.domain.crawl.CrawledNotice
import com.fx.crawler.domain.crawl.NoticeDetail
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.web.client.RestClient
import java.io.ByteArrayInputStream
import java.net.URI
import java.time.Clock
import java.time.LocalDate

/**
 * 학교 게시판 크롤링. 연결 · 읽기 타임아웃은 `spring.http.clients.*` 설정을 따른다.
 * 응답 문자셋은 Jsoup 이 HTML `meta` 에서 판단하도록 바이트로 받는다.
 * 게시판 링크는 이미 인코딩된 값이므로 URI 템플릿으로 다시 인코딩하지 않고 그대로 요청한다.
 */
@CrawlAdapter
class NoticeCrawlAdapter(
    restClientBuilder: RestClient.Builder,
    private val clock: Clock,
) : NoticeCrawlPort {

    private val restClient: RestClient = restClientBuilder.build()

    override fun fetchNoticeList(topic: TopicView): List<CrawledNotice> =
        NoticeHtmlParser.parseList(fetch(topic.noticeUrl() + "?pageIndex=1"), topic, LocalDate.now(clock))

    override fun fetchNoticeDetail(contentUrl: String): NoticeDetail =
        NoticeHtmlParser.parseDetail(fetch(contentUrl))

    private fun fetch(url: String): Document {
        val body = restClient.get()
            .uri(URI.create(url))
            .retrieve()
            .body(ByteArray::class.java)
            ?: ByteArray(0)
        return Jsoup.parse(ByteArrayInputStream(body), null, url)
    }

}
