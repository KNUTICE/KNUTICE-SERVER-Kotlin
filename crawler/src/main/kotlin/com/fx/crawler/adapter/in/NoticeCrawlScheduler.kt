package com.fx.crawler.adapter.`in`

import com.fx.crawler.appllication.port.`in`.NoticeCrawlUseCase
import com.fx.crawler.common.annotation.ScheduleAdapter
import com.fx.crawler.domain.NoticeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

@ScheduleAdapter
class NoticeCrawlScheduler(
    private val noticeCrawlUseCase: NoticeCrawlUseCase
) {

    private val log = LoggerFactory.getLogger(NoticeCrawlScheduler::class.java)

        @Scheduled(cron = "0 0/15 * * * *")
//    @Scheduled(fixedRate = 60_000) // 60,000ms = 1분
    fun crawlAndPushNotices() {
        log.info("Starting scheduled notice crawling")

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val newNotices = noticeCrawlUseCase.crawlAndSaveNotices(NoticeType.entries)
                log.info("Scheduled crawl finished")
            } catch (e: Exception) {
                log.error("Error during scheduled notice crawling", e)
            }
        }

    }


}