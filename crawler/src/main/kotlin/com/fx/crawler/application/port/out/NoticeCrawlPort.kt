package com.fx.crawler.application.port.out

import com.fx.common.domain.catalog.TopicView
import com.fx.crawler.domain.crawl.CrawledNotice
import com.fx.crawler.domain.crawl.NoticeDetail

/** 학교 게시판 크롤링. */
interface NoticeCrawlPort {

    /** 게시판 첫 페이지의 공지 목록. */
    fun fetchNoticeList(topic: TopicView): List<CrawledNotice>

    fun fetchNoticeDetail(contentUrl: String): NoticeDetail

}
