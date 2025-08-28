package com.fx.crawler.adapter.`in`

import com.fx.crawler.adapter.out.crawler.NoticeCrawlAdapter
import com.fx.crawler.appllication.service.NoticeCrawlService
import com.fx.crawler.domain.NoticeType
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.time.measureTimedValue

@RestController
class TestController(
    private val noticeCrawlAdapter: NoticeCrawlAdapter,
    private val noticeCrawlService: NoticeCrawlService
) {

    @GetMapping("/test")
    fun test() = runBlocking {
        val result = measureTimedValue {
            noticeCrawlService.crawlAndSaveNotices(NoticeType.values().toList())

        }
        println("new notices : ${result.value.size}")
        println("실행 시간: ${result.duration.inWholeMilliseconds} ms")

    }

}